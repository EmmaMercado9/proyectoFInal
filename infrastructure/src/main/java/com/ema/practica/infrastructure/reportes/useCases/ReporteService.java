package com.ema.practica.infrastructure.reportes.useCases;

import com.ema.practica.infrastructure.detailOrder.entity.OrderDetailEntity;
import com.ema.practica.infrastructure.devolucion.crud.DevolucionRepository;
import com.ema.practica.infrastructure.devolucion.entity.DevolucionDetalleEntity;
import com.ema.practica.infrastructure.devolucion.entity.DevolucionEntity;
import com.ema.practica.infrastructure.exception.VentaInvalidaException;
import com.ema.practica.infrastructure.facturacion.config.ArcaProperties;
import com.ema.practica.infrastructure.facturacion.crud.FacturaRepository;
import com.ema.practica.infrastructure.facturacion.entity.FacturaEntity;
import com.ema.practica.infrastructure.facturacion.entity.TipoComprobante;
import com.ema.practica.infrastructure.importacion.crud.FacturaImportadaRepository;
import com.ema.practica.infrastructure.importacion.entity.FacturaImportadaEntity;
import com.ema.practica.infrastructure.moduloProduct.product.entity.ProductEntity;
import com.ema.practica.infrastructure.moduloProduct.productVariant.crud.ProductVariantRepository;
import com.ema.practica.infrastructure.moduloProduct.productVariant.entity.ProductVariantEntity;
import com.ema.practica.infrastructure.order.crud.OrderRepository;
import com.ema.practica.infrastructure.order.entity.OrderEntity;
import com.ema.practica.infrastructure.order.entity.PagoVentaEntity;
import com.ema.practica.infrastructure.order.enumOrder.OrderStatus;
import com.ema.practica.infrastructure.order.enumOrder.PaymentMethod;
import com.ema.practica.infrastructure.reportes.rest.model.ReporteDTO;
import com.ema.practica.infrastructure.reportes.rest.model.ReporteDTO.*;
import com.ema.practica.infrastructure.util.status.StatusEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Estadísticas y reportes de gestión. Todo se calcula a partir de los datos que ya
 * registra el sistema (ventas, devoluciones, costos, stock, facturas):
 * <ul>
 *   <li>Ventas netas = lo cobrado − lo devuelto. Las devoluciones se descuentan de la venta original.</li>
 *   <li>Ganancia bruta = ventas netas − costo de lo vendido − costo de los productos fallados devueltos.</li>
 *   <li>Las recomendaciones son reglas sobre la velocidad de venta de los últimos 30 y 90 días.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class ReporteService {

    /** Ventas que cuentan como vendidas (las canceladas y pendientes no). */
    static final Set<OrderStatus> CUENTAN = EnumSet.of(
            OrderStatus.COMPLETADA, OrderStatus.DEVUELTA_PARCIAL, OrderStatus.DEVUELTA, OrderStatus.EN_ENVIO);
    static final int DIAS_ROTACION = 30;        // ventana para la velocidad de venta
    static final int DIAS_SIN_ROTACION = 90;    // sin ventas en este plazo = mercadería quieta
    static final double COBERTURA_MINIMA = 15;  // reponer si el stock alcanza para menos días
    static final int DIAS_A_CUBRIR = 30;        // la compra sugerida cubre un mes de ventas
    static final double MARGEN_BAJO = 20;       // %
    static final double TASA_DEVOLUCION_ALTA = 0.2;
    static final int MAX_DIAS = 3 * 366;
    private static final Locale AR = new Locale("es", "AR");

    private final OrderRepository orderRepository;
    private final DevolucionRepository devolucionRepository;
    private final ProductVariantRepository variantRepository;
    private final FacturaRepository facturaRepository;
    private final FacturaImportadaRepository facturaImportadaRepository;
    private final ArcaProperties arcaProperties;

    /** Ventas vigentes de los últimos 7 días sin Factura C en el ambiente actual. */
    @Transactional(readOnly = true)
    public long ventasSinFacturar() {
        return orderRepository.contarSinFacturar(
                EnumSet.of(OrderStatus.COMPLETADA, OrderStatus.DEVUELTA_PARCIAL),
                LocalDate.now().minusDays(6).atStartOfDay(),
                arcaProperties.getAmbiente().name());
    }

    @Transactional(readOnly = true)
    public ReporteDTO generar(LocalDate desde, LocalDate hasta) {
        return generar(desde, hasta, LocalDate.now());
    }

    ReporteDTO generar(LocalDate desde, LocalDate hasta, LocalDate hoy) {
        if (desde.isAfter(hasta)) {
            throw new VentaInvalidaException("La fecha \"desde\" no puede ser posterior a \"hasta\".");
        }
        long dias = ChronoUnit.DAYS.between(desde, hasta) + 1;
        if (dias > MAX_DIAS) {
            throw new VentaInvalidaException("El período no puede superar los 3 años.");
        }
        Ventas actual = cargar(desde, hasta);
        Ventas anterior = cargar(desde.minusDays(dias), desde.minusDays(1));

        Resumen resumen = resumen(actual);
        List<Producto> porProducto = porProducto(actual.lineas);
        List<Item> porPago = porMedioDePago(actual);
        List<Item> porDia = porDiaDeSemana(actual);
        List<Item> porCategoria = porLinea(actual.lineas, l -> categoria(l.variante.getProduct()));
        List<Item> porMarca = porLinea(actual.lineas, l -> Optional.ofNullable(l.variante.getProduct().getMarca())
                .filter(m -> !m.isBlank()).orElse("Sin marca"));
        Perdidas perdidas = perdidas(actual, resumen);
        Inventario inventario = inventario();
        Facturacion facturacion = facturacion(desde, hasta, resumen.ventasNetas());
        Recomendaciones recomendaciones = recomendaciones(hoy, porProducto);

        List<Producto> masVendidos = porProducto.stream()
                .sorted(Comparator.comparingInt(Producto::unidades).reversed()).limit(10).toList();
        List<Producto> masRentables = porProducto.stream()
                .sorted(Comparator.comparing(Producto::ganancia).reversed()).limit(10).toList();

        return new ReporteDTO(desde, hasta, resumen, resumen(anterior),
                ventasPorDia(actual, desde, hasta), porPago,
                porOrden(actual, o -> o.getUser() != null ? o.getUser().getName() : "Sin vendedor"),
                porCategoria, porMarca, porDia, porHora(actual),
                masVendidos, masRentables, perdidas, inventario, compras(desde, hasta), facturacion,
                recomendaciones,
                observaciones(resumen, resumen(anterior), dias, porPago, porDia, masRentables, perdidas,
                        recomendaciones, facturacion, actual.costosEstimados),
                actual.costosEstimados);
    }

    // ─── Carga ────────────────────────────────────────────────────────────────

    /** Un renglón vendido, ya neto de devoluciones. */
    record Linea(OrderEntity orden, OrderDetailEntity detalle, ProductVariantEntity variante,
                 int unidades, BigDecimal ingreso, BigDecimal costo) {
        BigDecimal ganancia() {
            return ingreso.subtract(costo);
        }
    }

    /** Ventas de un período con todo lo necesario para los cálculos. */
    static class Ventas {
        List<OrderEntity> todas = List.of();
        List<OrderEntity> validas = List.of();
        List<DevolucionEntity> devoluciones = List.of();
        List<Linea> lineas = new ArrayList<>();
        Map<Long, BigDecimal> devueltoPorOrden = new HashMap<>();
        Map<Long, BigDecimal> falladosPorOrden = new HashMap<>();
        int unidadesFalladas;
        int costosEstimados;

        BigDecimal netaDe(OrderEntity o) {
            return nz(o.getTotalAmount()).subtract(devueltoPorOrden.getOrDefault(o.getIdOrder(), BigDecimal.ZERO));
        }

        BigDecimal gananciaDe(OrderEntity o) {
            BigDecimal costo = lineas.stream().filter(l -> l.orden == o).map(Linea::costo)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return netaDe(o).subtract(costo).subtract(falladosPorOrden.getOrDefault(o.getIdOrder(), BigDecimal.ZERO));
        }
    }

    Ventas cargar(LocalDate desde, LocalDate hasta) {
        Ventas v = new Ventas();
        v.todas = orderRepository.findByFechaBetween(desde.atStartOfDay(), hasta.atTime(LocalTime.MAX));
        v.validas = v.todas.stream().filter(o -> CUENTAN.contains(o.getStatus())).toList();
        if (v.validas.isEmpty()) {
            return v;
        }
        v.devoluciones = devolucionRepository.findByOrder_IdOrderIn(
                v.validas.stream().map(OrderEntity::getIdOrder).toList());
        for (DevolucionEntity d : v.devoluciones) {
            Long id = d.getOrder().getIdOrder();
            v.devueltoPorOrden.merge(id, nz(d.getMontoDevuelto()), BigDecimal::add);
            for (DevolucionDetalleEntity item : d.getItems()) {
                if (Boolean.FALSE.equals(item.getReingresaStock())) {     // fallado: no vuelve al stock
                    BigDecimal perdida = costoUnitario(v, item.getOrderDetail(), false)
                            .multiply(BigDecimal.valueOf(item.getQuantity()));
                    v.falladosPorOrden.merge(id, perdida, BigDecimal::add);
                    v.unidadesFalladas += item.getQuantity();
                }
            }
        }
        for (OrderEntity o : v.validas) {
            // El descuento de la venta se reparte proporcionalmente entre sus renglones
            BigDecimal factor = nz(o.getSubtotal()).signum() > 0
                    ? nz(o.getTotalAmount()).divide(o.getSubtotal(), 6, RoundingMode.HALF_UP)
                    : BigDecimal.ONE;
            for (OrderDetailEntity d : o.getDetails()) {
                int netas = d.getCantidadDevolvible();
                if (netas <= 0) continue;
                BigDecimal ingreso = d.getPriceUnit().multiply(BigDecimal.valueOf(netas)).multiply(factor)
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal costo = costoUnitario(v, d, true).multiply(BigDecimal.valueOf(netas));
                v.lineas.add(new Linea(o, d, d.getVariant(), netas, ingreso, costo));
            }
        }
        return v;
    }

    /** Costo guardado en la venta; si es una venta anterior a ese dato, el costo actual. */
    private static BigDecimal costoUnitario(Ventas v, OrderDetailEntity d, boolean contar) {
        if (d.getCostoUnitario() != null) {
            return d.getCostoUnitario();
        }
        if (contar) v.costosEstimados++;
        return nz(d.getVariant().getCostoEfectivo());
    }

    // ─── Resumen ──────────────────────────────────────────────────────────────

    Resumen resumen(Ventas v) {
        BigDecimal brutas = suma(v.validas, OrderEntity::getSubtotal);
        // Descuento manual + descuento por medio de pago (efectivo); los recargos de cuotas van aparte
        BigDecimal recargos = BigDecimal.ZERO, descuentoMedio = BigDecimal.ZERO;
        for (OrderEntity o : v.validas) {
            List<BigDecimal> ajustes = o.getPagos().isEmpty()
                    ? List.of(o.getAjusteMedioPagoSeguro())
                    : o.getPagos().stream().map(PagoVentaEntity::getAjuste).toList();
            for (BigDecimal a : ajustes) {
                if (a.signum() > 0) recargos = recargos.add(a);
                else descuentoMedio = descuentoMedio.subtract(a);
            }
        }
        BigDecimal descuentos = suma(v.validas, OrderEntity::getDiscount).add(descuentoMedio);
        BigDecimal devoluciones = suma(v.devoluciones, DevolucionEntity::getMontoDevuelto);
        BigDecimal netas = suma(v.validas, OrderEntity::getTotalAmount).subtract(devoluciones);
        BigDecimal costo = suma(v.lineas, Linea::costo);
        BigDecimal fallados = v.falladosPorOrden.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal ganancia = netas.subtract(costo).subtract(fallados);
        int cantidad = v.validas.size();
        List<OrderEntity> canceladas = v.todas.stream().filter(o -> o.getStatus() == OrderStatus.CANCELADA).toList();
        return new Resumen(brutas, descuentos, recargos, devoluciones, netas, costo, fallados, ganancia,
                porcentaje(ganancia, netas), cantidad, v.lineas.stream().mapToInt(Linea::unidades).sum(),
                cantidad > 0 ? netas.divide(BigDecimal.valueOf(cantidad), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO,
                canceladas.size(), suma(canceladas, OrderEntity::getTotalAmount));
    }

    // ─── Desgloses ────────────────────────────────────────────────────────────

    List<Punto> ventasPorDia(Ventas v, LocalDate desde, LocalDate hasta) {
        Map<LocalDate, List<OrderEntity>> porFecha = v.validas.stream()
                .collect(Collectors.groupingBy(o -> o.getOrderDate().toLocalDate()));
        List<Punto> puntos = new ArrayList<>();
        for (LocalDate d = desde; !d.isAfter(hasta); d = d.plusDays(1)) {
            List<OrderEntity> delDia = porFecha.getOrDefault(d, List.of());
            puntos.add(new Punto(d, suma(delDia, v::netaDe), suma(delDia, v::gananciaDe), delDia.size()));
        }
        return puntos;
    }

    /** Agrupa ventas enteras (medio de pago, vendedor…), ordenadas de mayor a menor. */
    /** Un pago combinado reparte la venta entre sus medios, según lo cobrado con cada uno. */
    List<Item> porMedioDePago(Ventas v) {
        Map<String, Item> porMedio = new LinkedHashMap<>();
        for (OrderEntity o : v.validas) {
            if (o.getPagos().isEmpty()) {
                sumarMedio(porMedio, etiqueta(o.getPayment()), v.netaDe(o), v.gananciaDe(o));
                continue;
            }
            for (PagoVentaEntity p : o.getPagos()) {
                BigDecimal parte = nz(o.getTotalAmount()).signum() > 0
                        ? p.getTotal().divide(o.getTotalAmount(), 6, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;
                sumarMedio(porMedio, etiqueta(p.getMedio()),
                        v.netaDe(o).multiply(parte).setScale(2, RoundingMode.HALF_UP),
                        v.gananciaDe(o).multiply(parte).setScale(2, RoundingMode.HALF_UP));
            }
        }
        return porMedio.values().stream().sorted(Comparator.comparing(Item::ventas).reversed()).toList();
    }

    private static void sumarMedio(Map<String, Item> porMedio, String medio, BigDecimal ventas, BigDecimal ganancia) {
        porMedio.merge(medio, new Item(medio, ventas, ganancia, 1),
                (a, b) -> new Item(medio, a.ventas().add(b.ventas()), a.ganancia().add(b.ganancia()), a.cantidad() + 1));
    }

    List<Item> porOrden(Ventas v, Function<OrderEntity, String> clave) {
        return v.validas.stream().collect(Collectors.groupingBy(clave, LinkedHashMap::new, Collectors.toList()))
                .entrySet().stream()
                .map(e -> new Item(e.getKey(), suma(e.getValue(), v::netaDe), suma(e.getValue(), v::gananciaDe),
                        e.getValue().size()))
                .sorted(Comparator.comparing(Item::ventas).reversed())
                .toList();
    }

    /** Agrupa renglones vendidos (categoría, marca…); la cantidad es en unidades. */
    List<Item> porLinea(List<Linea> lineas, Function<Linea, String> clave) {
        return lineas.stream().collect(Collectors.groupingBy(clave, LinkedHashMap::new, Collectors.toList()))
                .entrySet().stream()
                .map(e -> new Item(e.getKey(), suma(e.getValue(), Linea::ingreso),
                        suma(e.getValue(), Linea::ganancia), e.getValue().stream().mapToInt(Linea::unidades).sum()))
                .sorted(Comparator.comparing(Item::ventas).reversed())
                .toList();
    }

    List<Item> porDiaDeSemana(Ventas v) {
        Map<DayOfWeek, List<OrderEntity>> porDia = v.validas.stream()
                .collect(Collectors.groupingBy(o -> o.getOrderDate().getDayOfWeek()));
        List<Item> items = new ArrayList<>();
        for (DayOfWeek dia : DayOfWeek.values()) {       // lunes a domingo, en orden
            List<OrderEntity> ordenes = porDia.getOrDefault(dia, List.of());
            items.add(new Item(capitalizar(dia.getDisplayName(TextStyle.FULL, AR)), suma(ordenes, v::netaDe),
                    suma(ordenes, v::gananciaDe), ordenes.size()));
        }
        return items;
    }

    List<Item> porHora(Ventas v) {
        Map<Integer, List<OrderEntity>> porHora = v.validas.stream()
                .collect(Collectors.groupingBy(o -> o.getOrderDate().getHour(), TreeMap::new, Collectors.toList()));
        return porHora.entrySet().stream()
                .map(e -> new Item(String.format("%02d h", e.getKey()), suma(e.getValue(), v::netaDe),
                        suma(e.getValue(), v::gananciaDe), e.getValue().size()))
                .toList();
    }

    List<Producto> porProducto(List<Linea> lineas) {
        return lineas.stream()
                .collect(Collectors.groupingBy(l -> l.variante.getProduct(), LinkedHashMap::new, Collectors.toList()))
                .entrySet().stream()
                .map(e -> {
                    BigDecimal ventas = suma(e.getValue(), Linea::ingreso);
                    BigDecimal ganancia = suma(e.getValue(), Linea::ganancia);
                    return new Producto(e.getKey().getNameProduct(), e.getKey().getSku(),
                            e.getValue().stream().mapToInt(Linea::unidades).sum(), ventas, ganancia,
                            porcentaje(ganancia, ventas));
                })
                .toList();
    }

    // ─── Pérdidas ─────────────────────────────────────────────────────────────

    Perdidas perdidas(Ventas v, Resumen r) {
        // Renglones vendidos por debajo de su costo (por precio o por descuento)
        List<Linea> bajoCosto = v.lineas.stream().filter(l -> l.ganancia().signum() < 0).toList();
        BigDecimal perdidaBajoCosto = suma(bajoCosto, l -> l.ganancia().negate());
        List<Producto> productos = porProducto(bajoCosto).stream()
                .sorted(Comparator.comparing(Producto::ganancia)).toList();
        return new Perdidas(r.perdidaFallados(), v.unidadesFalladas, perdidaBajoCosto, productos,
                r.descuentos(), r.devoluciones(), r.canceladas(), r.montoCancelado());
    }

    // ─── Inventario, compras y facturación ───────────────────────────────────

    /** Variantes que forman el stock vendible (activas, sin las "por defecto" de productos con talles). */
    List<ProductVariantEntity> stockVendible() {
        return variantRepository.findAll().stream()
                .filter(v -> v.getStatus() == StatusEntity.ACTIVO)
                .filter(v -> v.getProduct().getStatus() == StatusEntity.ACTIVO)
                .filter(v -> !(Boolean.TRUE.equals(v.getEsDefault()) && Boolean.TRUE.equals(v.getProduct().getTieneVariantes())))
                .toList();
    }

    Inventario inventario() {
        List<ProductVariantEntity> variantes = stockVendible();
        int unidades = 0, sinStock = 0, bajoMinimo = 0;
        BigDecimal alCosto = BigDecimal.ZERO, alPrecio = BigDecimal.ZERO;
        for (ProductVariantEntity v : variantes) {
            int stock = Math.max(0, nzi(v.getStock()));
            unidades += stock;
            alCosto = alCosto.add(nz(v.getCostoEfectivo()).multiply(BigDecimal.valueOf(stock)));
            alPrecio = alPrecio.add(nz(v.getPrecioEfectivo()).multiply(BigDecimal.valueOf(stock)));
            if (stock == 0) sinStock++;
            else if (stock <= nzi(v.getStockMinimo())) bajoMinimo++;
        }
        return new Inventario(unidades, alCosto, alPrecio, sinStock, bajoMinimo);
    }

    Compras compras(LocalDate desde, LocalDate hasta) {
        List<FacturaImportadaEntity> facturas = facturaImportadaRepository.findByFechaBetween(desde, hasta);
        return new Compras(facturas.size(),
                facturas.stream().mapToInt(f -> nzi(f.getUnidades())).sum(),
                suma(facturas, FacturaImportadaEntity::getTotal));
    }

    Facturacion facturacion(LocalDate desde, LocalDate hasta, BigDecimal ventasNetas) {
        String ambiente = arcaProperties.getAmbiente().name();
        List<FacturaEntity> comprobantes = facturaRepository.findByFechaBetweenAndAmbiente(desde, hasta, ambiente);
        BigDecimal facturado = suma(comprobantes.stream().filter(f -> f.getTipo() == TipoComprobante.FACTURA_C).toList(),
                FacturaEntity::getImporteTotal);
        BigDecimal nc = suma(comprobantes.stream().filter(f -> f.getTipo() == TipoComprobante.NOTA_CREDITO_C).toList(),
                FacturaEntity::getImporteTotal);
        BigDecimal neto = facturado.subtract(nc);
        return new Facturacion(ambiente, comprobantes.size(), facturado, nc, neto, porcentaje(neto, ventasNetas));
    }

    // ─── Recomendaciones ──────────────────────────────────────────────────────

    Recomendaciones recomendaciones(LocalDate hoy, List<Producto> productosDelPeriodo) {
        Ventas ultimos90 = cargar(hoy.minusDays(DIAS_SIN_ROTACION - 1), hoy);
        LocalDate inicio30 = hoy.minusDays(DIAS_ROTACION - 1);

        Map<Long, Integer> vendidas30 = new HashMap<>();
        Map<Long, Integer> vendidas90 = new HashMap<>();
        for (Linea l : ultimos90.lineas) {
            Long id = l.variante.getVariantId();
            vendidas90.merge(id, l.unidades, Integer::sum);
            if (!l.orden.getOrderDate().toLocalDate().isBefore(inicio30)) {
                vendidas30.merge(id, l.unidades, Integer::sum);
            }
        }

        List<Reposicion> reponer = new ArrayList<>();
        List<SinRotacion> sinRotacion = new ArrayList<>();
        for (ProductVariantEntity v : stockVendible()) {
            int stock = Math.max(0, nzi(v.getStock()));
            int v30 = vendidas30.getOrDefault(v.getVariantId(), 0);
            if (v30 > 0) {
                double porDia = v30 / (double) DIAS_ROTACION;
                double cobertura = stock / porDia;
                boolean urgente = stock == 0;
                // Bajo el mínimo solo cuenta si además no alcanza para el mes (si rota poco, no hace falta comprar)
                boolean bajoMinimo = stock <= nzi(v.getStockMinimo()) && cobertura < DIAS_A_CUBRIR;
                if (urgente || cobertura < COBERTURA_MINIMA || bajoMinimo) {
                    int sugerido = Math.max(1, (int) Math.ceil(porDia * DIAS_A_CUBRIR) - stock);
                    reponer.add(new Reposicion(v.getProduct().getNameProduct(), variante(v), v.getSku(), stock, v30,
                            redondear(cobertura), sugerido,
                            nz(v.getCostoEfectivo()).multiply(BigDecimal.valueOf(sugerido)), urgente));
                }
            } else if (stock > 0 && vendidas90.getOrDefault(v.getVariantId(), 0) == 0) {
                sinRotacion.add(new SinRotacion(v.getProduct().getNameProduct(), variante(v), v.getSku(), stock,
                        nz(v.getCostoEfectivo()).multiply(BigDecimal.valueOf(stock))));
            }
        }
        reponer.sort(Comparator.comparing(Reposicion::urgente).reversed()
                .thenComparing(Comparator.comparingInt(Reposicion::vendidas30).reversed()));
        sinRotacion.sort(Comparator.comparing(SinRotacion::capitalInmovilizado).reversed());

        List<Producto> margenBajo = productosDelPeriodo.stream()
                .filter(p -> p.margen() != null && p.margen() < MARGEN_BAJO)
                .sorted(Comparator.comparing(Producto::margen)).limit(10).toList();

        return new Recomendaciones(limitar(reponer, 20), limitar(sinRotacion, 15), margenBajo,
                altaDevolucion(ultimos90), tallesMasVendidos(ultimos90));
    }

    /** Productos que se devuelven mucho (al menos 3 vendidos y 20 % devuelto): revisar calidad o talles. */
    List<AltaDevolucion> altaDevolucion(Ventas v) {
        Map<ProductEntity, int[]> porProducto = new LinkedHashMap<>();   // [vendidas, devueltas]
        for (OrderEntity o : v.validas) {
            for (OrderDetailEntity d : o.getDetails()) {
                int[] c = porProducto.computeIfAbsent(d.getVariant().getProduct(), k -> new int[2]);
                c[0] += d.getQuantity();
                c[1] += d.getCantidadDevueltaSegura();
            }
        }
        return porProducto.entrySet().stream()
                .filter(e -> e.getValue()[0] >= 3 && e.getValue()[1] / (double) e.getValue()[0] >= TASA_DEVOLUCION_ALTA)
                .map(e -> new AltaDevolucion(e.getKey().getNameProduct(), e.getValue()[0], e.getValue()[1],
                        redondear(e.getValue()[1] * 100.0 / e.getValue()[0])))
                .sorted(Comparator.comparingDouble(AltaDevolucion::tasa).reversed())
                .toList();
    }

    /** Qué talles se venden más: orienta qué curva de talles comprar. */
    List<TalleTop> tallesMasVendidos(Ventas v) {
        Map<String, Integer> conteo = new LinkedHashMap<>();
        Map<String, String[]> datos = new HashMap<>();
        for (Linea l : v.lineas) {
            String talle = talle(l.variante);
            if (talle == null) continue;
            String clave = l.variante.getProduct().getIdProduct() + "|" + talle;
            conteo.merge(clave, l.unidades, Integer::sum);
            datos.putIfAbsent(clave, new String[]{l.variante.getProduct().getNameProduct(), talle});
        }
        return conteo.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .map(e -> new TalleTop(datos.get(e.getKey())[0], datos.get(e.getKey())[1], e.getValue()))
                .toList();
    }

    // ─── Observaciones en lenguaje natural ────────────────────────────────────

    List<String> observaciones(Resumen r, Resumen ant, long dias, List<Item> porPago, List<Item> porDia,
                               List<Producto> masRentables, Perdidas perdidas, Recomendaciones rec,
                               Facturacion fac, int costosEstimados) {
        List<String> obs = new ArrayList<>();
        if (r.cantidadVentas() == 0) {
            obs.add("No hubo ventas en el período elegido.");
        } else {
            if (ant.ventasNetas().signum() > 0) {
                double variacion = r.ventasNetas().subtract(ant.ventasNetas())
                        .multiply(BigDecimal.valueOf(100)).divide(ant.ventasNetas(), 1, RoundingMode.HALF_UP).doubleValue();
                obs.add(String.format(AR, "Las ventas netas %s %.1f %% respecto de los %d días anteriores (%s contra %s).",
                        variacion >= 0 ? "subieron" : "bajaron", Math.abs(variacion), dias,
                        pesos(r.ventasNetas()), pesos(ant.ventasNetas())));
            }
            if (r.margen() != null) {
                obs.add(String.format(AR, "Por cada $100 vendidos quedaron $%.0f de ganancia bruta.", r.margen()));
            }
            porDia.stream().max(Comparator.comparing(Item::ventas)).filter(i -> i.ventas().signum() > 0).ifPresent(i ->
                    obs.add(String.format(AR, "El día que más vendés es el %s (%.0f %% de las ventas): asegurá stock y personal ese día.",
                            i.nombre().toLowerCase(AR), porcentajeDouble(i.ventas(), r.ventasNetas()))));
            porPago.stream().findFirst().ifPresent(i ->
                    obs.add(String.format(AR, "El medio de pago más usado es %s (%.0f %% de las ventas).",
                            i.nombre(), porcentajeDouble(i.ventas(), r.ventasNetas()))));
            masRentables.stream().findFirst().filter(p -> p.ganancia().signum() > 0).ifPresent(p ->
                    obs.add("El producto que más ganancia dejó es " + p.producto() + " (" + pesos(p.ganancia()) + ")."));
        }
        long urgentes = rec.reponer().stream().filter(Reposicion::urgente).count();
        if (urgentes > 0) {
            obs.add("Hay " + urgentes + " talle(s) sin stock que se vendieron en los últimos 30 días: reponelos primero.");
        }
        BigDecimal quieto = suma(rec.sinRotacion(), SinRotacion::capitalInmovilizado);
        if (quieto.signum() > 0) {
            obs.add("Tenés " + pesos(quieto) + " en mercadería sin ventas en 90 días (" + rec.sinRotacion().size()
                    + " variantes): considerá una promoción. Si son productos recién cargados, ignorá este aviso.");
        }
        if (perdidas.perdidaFallados().signum() > 0) {
            obs.add("Los productos fallados devueltos costaron " + pesos(perdidas.perdidaFallados()) + ".");
        }
        if (perdidas.perdidaVentasBajoCosto().signum() > 0) {
            obs.add("Hubo ventas por debajo del costo por " + pesos(perdidas.perdidaVentasBajoCosto())
                    + ": revisá precios y descuentos de esos productos.");
        }
        if ("HOMOLOGACION".equals(fac.ambiente())) {
            obs.add("La facturación electrónica está en modo prueba: los importes facturados no tienen validez fiscal.");
        } else if (fac.porcentajeDeVentas() != null && r.ventasNetas().signum() > 0) {
            obs.add(String.format(AR, "El %.0f %% de las ventas tiene factura electrónica.", fac.porcentajeDeVentas()));
        }
        if (costosEstimados > 0) {
            obs.add(costosEstimados + " renglones de ventas anteriores no tienen el costo guardado: "
                    + "para esos se usó el costo actual del producto.");
        }
        return obs;
    }

    // ─── Utilidades ───────────────────────────────────────────────────────────

    private static <T> BigDecimal suma(Collection<T> items, Function<T, BigDecimal> valor) {
        return items.stream().map(valor).map(ReporteService::nz).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal nz(BigDecimal b) {
        return b != null ? b : BigDecimal.ZERO;
    }

    private static int nzi(Integer i) {
        return i != null ? i : 0;
    }

    /** parte / total × 100, con un decimal; null si no hay total. */
    static Double porcentaje(BigDecimal parte, BigDecimal total) {
        if (total == null || total.signum() == 0) return null;
        return parte.multiply(BigDecimal.valueOf(100)).divide(total, 1, RoundingMode.HALF_UP).doubleValue();
    }

    private static double porcentajeDouble(BigDecimal parte, BigDecimal total) {
        Double p = porcentaje(parte, total);
        return p != null ? p : 0;
    }

    private static double redondear(double d) {
        return Math.round(d * 10) / 10.0;
    }

    private static <T> List<T> limitar(List<T> lista, int max) {
        return lista.size() > max ? lista.subList(0, max) : lista;
    }

    private static String pesos(BigDecimal b) {
        return "$" + String.format(AR, "%,.0f", b);
    }

    private static String capitalizar(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static String categoria(ProductEntity p) {
        if (p.getSubCategoria() == null || p.getSubCategoria().getCategoryProductEntity() == null) {
            return "Sin categoría";
        }
        return p.getSubCategoria().getCategoryProductEntity().getNameCategory();
    }

    private static String variante(ProductVariantEntity v) {
        String d = v.getDescripcionAtributos();
        return d == null || d.isBlank() ? "Único" : d;
    }

    private static String talle(ProductVariantEntity v) {
        return v.getAtributos().stream()
                .filter(a -> a.getAtributo().getNombre().equalsIgnoreCase("Talle")
                        || a.getAtributo().getNombre().equalsIgnoreCase("Talla"))
                .map(a -> a.getValor())
                .findFirst().orElse(null);
    }

    static String etiqueta(PaymentMethod p) {
        if (p == null) return "Sin dato";
        return switch (p) {
            case EFECTIVO -> "Efectivo";
            case TARJETA_DEBITO -> "Débito";
            case TARJETA_CREDITO -> "Crédito";
            case TRANSFERENCIA -> "Transferencia";
            case MERCADOPAGO -> "Mercado Pago";
        };
    }
}
