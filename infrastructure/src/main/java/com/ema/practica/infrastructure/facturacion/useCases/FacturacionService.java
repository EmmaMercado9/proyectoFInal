package com.ema.practica.infrastructure.facturacion.useCases;

import com.ema.practica.infrastructure.detailOrder.entity.OrderDetailEntity;
import com.ema.practica.infrastructure.devolucion.crud.DevolucionRepository;
import com.ema.practica.infrastructure.devolucion.entity.DevolucionEntity;
import com.ema.practica.infrastructure.exception.RecursoNoEncontradoException;
import com.ema.practica.infrastructure.exception.VentaInvalidaException;
import com.ema.practica.infrastructure.facturacion.arca.ArcaException;
import com.ema.practica.infrastructure.facturacion.arca.WsfeClient;
import com.ema.practica.infrastructure.facturacion.config.ArcaProperties;
import com.ema.practica.infrastructure.facturacion.crud.FacturaRepository;
import com.ema.practica.infrastructure.facturacion.entity.FacturaEntity;
import com.ema.practica.infrastructure.facturacion.entity.TipoComprobante;
import com.ema.practica.infrastructure.facturacion.rest.model.EstadoFacturacionDTO;
import com.ema.practica.infrastructure.facturacion.rest.model.FacturaResponseDTO;
import com.ema.practica.infrastructure.facturacion.rest.model.FacturarRequestDTO;
import com.ema.practica.infrastructure.facturacion.rest.model.NotaCreditoRequestDTO;
import com.ema.practica.infrastructure.moduloProduct.client.entity.ClientEntity;
import com.ema.practica.infrastructure.order.crud.OrderRepository;
import com.ema.practica.infrastructure.order.entity.OrderEntity;
import com.ema.practica.infrastructure.order.enumOrder.OrderStatus;
import com.ema.practica.infrastructure.util.user.entity.UserEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Factura C / Nota de Crédito C (emisor monotributista) con CAE de ARCA.
 * La numeración la lleva ARCA: se pide el último autorizado y se usa el
 * siguiente, de a uno por vez (lock) para no pisar números.
 */
@Service
@RequiredArgsConstructor
public class FacturacionService {

    public static final int DOC_CUIT = 80, DOC_CUIL = 86, DOC_DNI = 96, DOC_SIN_IDENTIFICAR = 99;
    public static final int COND_CONSUMIDOR_FINAL = 5;
    // RG 5700/2025: desde este importe hay que identificar al consumidor final
    static final BigDecimal TOPE_IDENTIFICACION = new BigDecimal("10000000");
    private static final String QR_URL = "https://www.arca.gob.ar/fe/qr/?p=";
    private static final ZoneId AR = ZoneId.of("America/Argentina/Buenos_Aires");

    static final Map<Integer, String> CONDICIONES_IVA = Map.of(
            1, "IVA Responsable Inscripto",
            4, "IVA Sujeto Exento",
            5, "Consumidor Final",
            6, "Responsable Monotributo",
            7, "Sujeto No Categorizado",
            13, "Monotributista Social",
            15, "IVA No Alcanzado",
            16, "Monotributo Trabajador Independiente Promovido");

    private final ArcaProperties props;
    private final ConfiguracionArcaService config;
    private final WsfeClient wsfe;
    private final FacturaRepository facturaRepository;
    private final OrderRepository orderRepository;
    private final DevolucionRepository devolucionRepository;
    private final ObjectMapper objectMapper;

    private final ReentrantLock numeracion = new ReentrantLock();

    // ── Estado / diagnóstico ────────────────────────────────────────────────

    public EstadoFacturacionDTO estado() {
        EstadoFacturacionDTO dto = new EstadoFacturacionDTO();
        List<String> faltantes = config.faltantes();
        ConfiguracionArcaService.Emisor emisor = config.emisor();
        dto.setHabilitada(faltantes.isEmpty());
        dto.setFaltantes(faltantes);
        dto.setAmbiente(props.getAmbiente().name());
        dto.setCuit(emisor.cuit());
        dto.setPuntoVenta(emisor.puntoVenta());
        dto.setRazonSocial(emisor.razonSocial());
        return dto;
    }

    /** Verifica servidores de ARCA y, si está configurado, el certificado (pide el último número) */
    public EstadoFacturacionDTO probarConexion() {
        EstadoFacturacionDTO dto = estado();
        if (props.produccionBloqueada()) {
            dto.setMensaje("Ambiente PRODUCCION bloqueado: no se contacta a ARCA. "
                    + "Usá ARCA_AMBIENTE=HOMOLOGACION para probar, o ARCA_PERMITIR_PRODUCCION=true para facturar de verdad.");
            return dto;
        }
        dto.setServidores(wsfe.dummy());
        if (dto.isHabilitada()) {
            long ultimo = wsfe.ultimoAutorizado(dto.getPuntoVenta(), TipoComprobante.FACTURA_C.getCodigo());
            dto.setMensaje("Certificado OK. Última Factura C autorizada en el punto de venta "
                    + dto.getPuntoVenta() + ": " + ultimo);
        } else {
            dto.setMensaje("Servidores de ARCA accesibles. Falta: " + String.join(" · ", dto.getFaltantes()));
        }
        return dto;
    }

    // ── Factura ─────────────────────────────────────────────────────────────

    @Transactional
    public FacturaResponseDTO facturar(Long idOrder, FacturarRequestDTO req, UserEntity usuario) {
        exigirConfiguracion();
        OrderEntity order = orderRepository.findById(idOrder)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden", idOrder));

        if (order.getStatus() != OrderStatus.COMPLETADA && order.getStatus() != OrderStatus.DEVUELTA_PARCIAL) {
            throw new VentaInvalidaException(order.getStatus() == OrderStatus.CANCELADA
                    ? "La venta está cancelada: no se puede facturar"
                    : "Solo se facturan ventas completadas");
        }
        if (facturaRepository.existsByOrder_IdOrderAndTipo(idOrder, TipoComprobante.FACTURA_C)) {
            throw new VentaInvalidaException("La venta #" + idOrder + " ya tiene factura");
        }

        // Si hubo devoluciones antes de facturar, se factura lo que efectivamente se cobró
        BigDecimal devuelto = devolucionRepository.sumMontoDevueltoByOrder(idOrder);
        BigDecimal importe = order.getTotalAmount().subtract(devuelto);
        if (importe.signum() <= 0) {
            throw new VentaInvalidaException("La venta no tiene importe a facturar");
        }

        Receptor receptor = resolverReceptor(req, order.getClientEntity(), importe);
        FacturaEntity f = autorizar(TipoComprobante.FACTURA_C, order, importe, receptor, null, usuario);
        return toDTO(f);
    }

    // ── Nota de crédito ─────────────────────────────────────────────────────

    @Transactional
    public FacturaResponseDTO notaCredito(Long idFactura, NotaCreditoRequestDTO req, UserEntity usuario) {
        exigirConfiguracion();
        FacturaEntity factura = facturaRepository.findById(idFactura)
                .orElseThrow(() -> new RecursoNoEncontradoException("Factura", idFactura));
        if (factura.getTipo() != TipoComprobante.FACTURA_C) {
            throw new VentaInvalidaException("Las notas de crédito se emiten sobre una factura");
        }
        if (!factura.getAmbiente().equals(props.getAmbiente().name())) {
            throw new VentaInvalidaException("La factura es del ambiente " + factura.getAmbiente()
                    + " y la facturación está en " + props.getAmbiente());
        }

        BigDecimal saldo = saldoAnulable(factura);
        DevolucionEntity devolucion = null;
        BigDecimal importe;
        if (req.getDevolucionId() != null) {
            devolucion = devolucionRepository.findById(req.getDevolucionId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Devolución", req.getDevolucionId()));
            if (!devolucion.getOrder().getIdOrder().equals(factura.getOrder().getIdOrder())) {
                throw new VentaInvalidaException("La devolución no corresponde a esta venta");
            }
            if (facturaRepository.existsByDevolucion_IdDevolucion(devolucion.getIdDevolucion())) {
                throw new VentaInvalidaException("Esa devolución ya tiene nota de crédito");
            }
            if (devolucion.getFecha().isBefore(factura.getCreadoEn())) {
                throw new VentaInvalidaException(
                        "Esa devolución es anterior a la factura y ya está descontada de su importe");
            }
            importe = devolucion.getMontoDevuelto();
        } else {
            importe = req.getImporte() != null ? req.getImporte() : saldo;
        }

        if (importe.signum() <= 0) {
            throw new VentaInvalidaException("No queda saldo para anular en la factura " + factura.getNumeroFormateado());
        }
        if (importe.compareTo(saldo) > 0) {
            throw new VentaInvalidaException("El importe ($" + importe + ") supera lo que queda por anular de la factura ($"
                    + saldo + ")");
        }

        Receptor receptor = new Receptor(factura.getDocTipo(), factura.getDocNro(), factura.getCondicionIvaReceptorId());
        FacturaEntity nc = autorizar(TipoComprobante.NOTA_CREDITO_C, factura.getOrder(), importe, receptor, factura, usuario);
        nc.setDevolucion(devolucion);
        nc.setMotivo(req.getMotivo() != null && !req.getMotivo().isBlank() ? req.getMotivo().trim() : null);
        return toDTO(nc);
    }

    // ── Consultas ───────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<FacturaResponseDTO> porOrden(Long idOrder) {
        return facturaRepository.findByOrder_IdOrderOrderByCreadoEnAsc(idOrder).stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public FacturaResponseDTO obtener(Long idFactura) {
        return toDTO(facturaRepository.findById(idFactura)
                .orElseThrow(() -> new RecursoNoEncontradoException("Factura", idFactura)));
    }

    // ── Núcleo: pedir CAE ───────────────────────────────────────────────────

    record Receptor(int docTipo, long docNro, int condicionIva) {}

    private FacturaEntity autorizar(TipoComprobante tipo, OrderEntity order, BigDecimal importe,
                                    Receptor receptor, FacturaEntity asociada, UserEntity usuario) {
        ConfiguracionArcaService.Emisor emisor = config.emisor();
        int ptoVta = emisor.puntoVenta();
        LocalDate hoy = LocalDate.now(AR);
        numeracion.lock();
        try {
            long numero = wsfe.ultimoAutorizado(ptoVta, tipo.getCodigo()) + 1;
            WsfeClient.CbteAsociado asoc = asociada == null ? null : new WsfeClient.CbteAsociado(
                    asociada.getTipo().getCodigo(), asociada.getPuntoVenta(), asociada.getNumero(),
                    asociada.getEmisorCuit() != null ? asociada.getEmisorCuit() : emisor.cuit(),
                    asociada.getFecha());

            WsfeClient.ResultadoCae res = wsfe.solicitarCae(new WsfeClient.SolicitudCae(
                    tipo.getCodigo(), ptoVta, numero, hoy, receptor.docTipo(), receptor.docNro(),
                    importe, receptor.condicionIva(), asoc));

            if (!res.aprobado()) {
                List<String> motivos = new ArrayList<>(res.errores());
                motivos.addAll(res.observaciones());
                throw new ArcaException("ARCA rechazó la " + tipo.getDescripcion() + ": "
                        + (motivos.isEmpty() ? "sin detalle" : String.join(" · ", motivos)));
            }

            FacturaEntity f = new FacturaEntity();
            f.setOrder(order);
            f.setTipo(tipo);
            f.setPuntoVenta(ptoVta);
            f.setNumero(numero);
            f.setFecha(hoy);
            f.setImporteTotal(importe);
            f.setDocTipo(receptor.docTipo());
            f.setDocNro(receptor.docNro());
            f.setCondicionIvaReceptorId(receptor.condicionIva());
            f.setCae(res.cae());
            f.setCaeVencimiento(res.caeVencimiento());
            f.setAmbiente(props.getAmbiente().name());
            f.setFacturaAsociada(asociada);
            f.setUsuario(usuario);
            f.setCreadoEn(LocalDateTime.now());
            f.setEmisorCuit(emisor.cuit());
            f.setEmisorRazonSocial(emisor.razonSocial());
            f.setEmisorDomicilio(emisor.domicilio());
            f.setEmisorIibb(emisor.iibb());
            f.setEmisorInicioActividades(emisor.inicioActividades());
            f.setEmisorCondicionIva(emisor.condicionIva());
            return facturaRepository.save(f);
        } finally {
            numeracion.unlock();
        }
    }

    // ── Reglas del receptor ─────────────────────────────────────────────────

    Receptor resolverReceptor(FacturarRequestDTO req, ClientEntity cliente, BigDecimal importe) {
        Integer docTipo = req != null ? req.getDocTipo() : null;
        Long docNro = req != null ? req.getDocNro() : null;
        Integer cond = req != null ? req.getCondicionIvaReceptorId() : null;

        // Por defecto: el DNI del cliente de la venta, o consumidor final sin identificar
        if (docTipo == null) {
            Long dni = soloDigitos(cliente != null ? cliente.getDni() : null);
            if (dni != null && dni > 0) {
                docTipo = DOC_DNI;
                docNro = dni;
            } else {
                docTipo = DOC_SIN_IDENTIFICAR;
                docNro = 0L;
            }
        }
        if (cond == null) cond = COND_CONSUMIDOR_FINAL;

        if (!CONDICIONES_IVA.containsKey(cond)) {
            throw new VentaInvalidaException("Condición frente al IVA del receptor inválida: " + cond);
        }
        switch (docTipo) {
            case DOC_SIN_IDENTIFICAR -> {
                if (cond != COND_CONSUMIDOR_FINAL) {
                    throw new VentaInvalidaException(
                            "Para facturar a un " + CONDICIONES_IVA.get(cond) + " hay que informar su CUIT");
                }
                if (importe.compareTo(TOPE_IDENTIFICACION) >= 0) {
                    throw new VentaInvalidaException(
                            "Ventas de $10.000.000 o más a consumidor final requieren DNI o CUIT del comprador");
                }
                docNro = 0L;
            }
            case DOC_DNI -> {
                if (docNro == null || docNro < 1_000_000L || docNro > 99_999_999L) {
                    throw new VentaInvalidaException("DNI inválido");
                }
            }
            case DOC_CUIT, DOC_CUIL -> {
                if (docNro == null || !cuitValido(docNro)) {
                    throw new VentaInvalidaException((docTipo == DOC_CUIT ? "CUIT" : "CUIL") + " inválido");
                }
            }
            default -> throw new VentaInvalidaException("Tipo de documento no soportado: " + docTipo);
        }
        return new Receptor(docTipo, docNro, cond);
    }

    /** Dígito verificador de CUIT/CUIL (módulo 11) */
    static boolean cuitValido(long cuit) {
        String s = String.valueOf(cuit);
        if (s.length() != 11) return false;
        int[] pesos = {5, 4, 3, 2, 7, 6, 5, 4, 3, 2};
        int suma = 0;
        for (int i = 0; i < 10; i++) suma += (s.charAt(i) - '0') * pesos[i];
        int dv = 11 - (suma % 11);
        if (dv == 11) dv = 0;
        if (dv == 10) return false;
        return dv == s.charAt(10) - '0';
    }

    private static Long soloDigitos(String s) {
        if (s == null) return null;
        String d = s.replaceAll("\\D", "");
        if (d.isEmpty() || d.length() > 11) return null;
        return Long.parseLong(d);
    }

    private void exigirConfiguracion() {
        List<String> faltantes = config.faltantes();
        if (!faltantes.isEmpty()) {
            throw new VentaInvalidaException(
                    "La facturación electrónica no está lista. Falta: " + String.join(" · ", faltantes));
        }
    }

    private BigDecimal saldoAnulable(FacturaEntity factura) {
        return factura.getImporteTotal().subtract(facturaRepository.sumNotasCredito(factura.getIdFactura()));
    }

    // ── Mapeo ───────────────────────────────────────────────────────────────

    FacturaResponseDTO toDTO(FacturaEntity f) {
        FacturaResponseDTO dto = new FacturaResponseDTO();
        dto.setIdFactura(f.getIdFactura());
        dto.setTipo(f.getTipo().name());
        dto.setTipoDescripcion(f.getTipo().getDescripcion());
        dto.setCodigoTipo(f.getTipo().getCodigo());
        dto.setPuntoVenta(f.getPuntoVenta());
        dto.setNumero(f.getNumero());
        dto.setNumeroFormateado(f.getNumeroFormateado());
        dto.setFecha(f.getFecha());
        dto.setImporteTotal(f.getImporteTotal());
        dto.setCae(f.getCae());
        dto.setCaeVencimiento(f.getCaeVencimiento());
        dto.setAmbiente(f.getAmbiente());
        dto.setPrueba(!"PRODUCCION".equals(f.getAmbiente()));
        dto.setDocTipo(f.getDocTipo());
        dto.setDocNro(f.getDocNro());
        dto.setCondicionIvaReceptorId(f.getCondicionIvaReceptorId());
        dto.setCondicionIvaReceptor(CONDICIONES_IVA.getOrDefault(f.getCondicionIvaReceptorId(), "—"));
        OrderEntity order = f.getOrder();
        dto.setIdOrder(order.getIdOrder());
        dto.setTrackingIdOrden(order.getTrackingId());
        dto.setReceptorNombre(order.getClientEntity() != null ? order.getClientEntity().getName() : null);
        if (f.getFacturaAsociada() != null) {
            dto.setIdFacturaAsociada(f.getFacturaAsociada().getIdFactura());
            dto.setFacturaAsociadaNumero(f.getFacturaAsociada().getNumeroFormateado());
        }
        if (f.getDevolucion() != null) dto.setIdDevolucion(f.getDevolucion().getIdDevolucion());
        dto.setMotivo(f.getMotivo());
        dto.setUsuario(f.getUsuario() != null ? f.getUsuario().getName() : null);
        dto.setCreadoEn(f.getCreadoEn());
        if (f.getTipo() == TipoComprobante.FACTURA_C && f.getIdFactura() != null) {
            dto.setSaldoAnulable(saldoAnulable(f));
        }

        // Datos del emisor tal como estaban al emitir
        FacturaResponseDTO.Emisor e = new FacturaResponseDTO.Emisor();
        e.setCuit(f.getEmisorCuit());
        e.setRazonSocial(f.getEmisorRazonSocial());
        e.setDomicilio(f.getEmisorDomicilio());
        e.setIibb(f.getEmisorIibb());
        e.setInicioActividades(f.getEmisorInicioActividades());
        e.setCondicionIva(f.getEmisorCondicionIva());
        dto.setEmisor(e);

        dto.setItems(items(f));
        dto.setQrUrl(qrUrl(f));
        return dto;
    }

    private List<FacturaResponseDTO.Item> items(FacturaEntity f) {
        List<FacturaResponseDTO.Item> items = new ArrayList<>();
        if (f.getTipo() == TipoComprobante.NOTA_CREDITO_C) {
            if (f.getDevolucion() != null) {
                f.getDevolucion().getItems().forEach(it -> {
                    var v = it.getOrderDetail().getVariant();
                    BigDecimal qty = BigDecimal.valueOf(it.getQuantity());
                    items.add(new FacturaResponseDTO.Item(
                            v.getProduct().getNameProduct() + " " + v.getDescripcionAtributos(),
                            qty, it.getMonto().divide(qty, 2, java.math.RoundingMode.HALF_UP), it.getMonto()));
                });
            } else {
                items.add(new FacturaResponseDTO.Item("Anulación " + (f.getFacturaAsociada() != null
                        ? "de Factura C " + f.getFacturaAsociada().getNumeroFormateado() : ""),
                        BigDecimal.ONE, f.getImporteTotal(), f.getImporteTotal()));
            }
            return items;
        }
        OrderEntity o = f.getOrder();
        for (OrderDetailEntity d : o.getDetails()) {
            var v = d.getVariant();
            String desc = v.getProduct().getNameProduct()
                    + (Boolean.TRUE.equals(v.getEsDefault()) ? "" : " " + v.getDescripcionAtributos());
            items.add(new FacturaResponseDTO.Item(desc.trim(), BigDecimal.valueOf(d.getQuantity()),
                    d.getPriceUnit(), d.getSubtotal()));
        }
        if (o.getDiscount() != null && o.getDiscount().signum() > 0) {
            items.add(new FacturaResponseDTO.Item("Descuento", BigDecimal.ONE,
                    o.getDiscount().negate(), o.getDiscount().negate()));
        }
        // Devoluciones previas a la factura: el importe facturado ya las descuenta
        BigDecimal ajuste = f.getImporteTotal().subtract(o.getTotalAmount());
        if (ajuste.signum() != 0) {
            items.add(new FacturaResponseDTO.Item("Devoluciones anteriores a la factura", BigDecimal.ONE, ajuste, ajuste));
        }
        return items;
    }

    /** QR según especificación de ARCA (RG 4892): URL + JSON en Base64 */
    String qrUrl(FacturaEntity f) {
        try {
            Map<String, Object> d = new LinkedHashMap<>();
            d.put("ver", 1);
            d.put("fecha", f.getFecha().toString());
            d.put("cuit", f.getEmisorCuit());
            d.put("ptoVta", f.getPuntoVenta());
            d.put("tipoCmp", f.getTipo().getCodigo());
            d.put("nroCmp", f.getNumero());
            d.put("importe", f.getImporteTotal());
            d.put("moneda", "PES");
            d.put("ctz", 1);
            if (f.getDocTipo() != DOC_SIN_IDENTIFICAR) {
                d.put("tipoDocRec", f.getDocTipo());
                d.put("nroDocRec", f.getDocNro());
            }
            d.put("tipoCodAut", "E");
            d.put("codAut", Long.parseLong(f.getCae()));
            String json = objectMapper.writeValueAsString(d);
            return QR_URL + Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return null;
        }
    }
}
