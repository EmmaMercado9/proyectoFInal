package com.ema.practica.infrastructure.facturacion.useCases;

import com.ema.practica.infrastructure.detailOrder.entity.OrderDetailEntity;
import com.ema.practica.infrastructure.devolucion.crud.DevolucionRepository;
import com.ema.practica.infrastructure.devolucion.entity.DevolucionEntity;
import com.ema.practica.infrastructure.exception.VentaInvalidaException;
import com.ema.practica.infrastructure.facturacion.arca.ArcaException;
import com.ema.practica.infrastructure.facturacion.arca.WsfeClient;
import com.ema.practica.infrastructure.facturacion.config.ArcaProperties;
import com.ema.practica.infrastructure.facturacion.crud.FacturaRepository;
import com.ema.practica.infrastructure.facturacion.entity.FacturaEntity;
import com.ema.practica.infrastructure.facturacion.entity.TipoComprobante;
import com.ema.practica.infrastructure.facturacion.rest.model.FacturarRequestDTO;
import com.ema.practica.infrastructure.facturacion.rest.model.NotaCreditoRequestDTO;
import com.ema.practica.infrastructure.moduloProduct.product.entity.ProductEntity;
import com.ema.practica.infrastructure.moduloProduct.productVariant.entity.ProductVariantEntity;
import com.ema.practica.infrastructure.order.crud.OrderRepository;
import com.ema.practica.infrastructure.order.entity.OrderEntity;
import com.ema.practica.infrastructure.order.enumOrder.OrderStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FacturacionServiceTest {

    @Mock WsfeClient wsfe;
    @Mock FacturaRepository facturaRepository;
    @Mock OrderRepository orderRepository;
    @Mock DevolucionRepository devolucionRepository;

    ArcaProperties props = new ArcaProperties();
    FacturacionService service;
    OrderEntity orden;

    @BeforeEach
    void setUp() {
        props.setEnabled(true);
        props.setCuit(20111111112L);
        props.setPuntoVenta(3);
        props.setCertPath("cert.crt");
        props.setKeyPath("clave.key");
        props.setRazonSocial("Tienda Test");
        service = new FacturacionService(props, wsfe, facturaRepository, orderRepository,
                devolucionRepository, new ObjectMapper());

        ProductEntity p = new ProductEntity();
        p.setNameProduct("Remera");
        ProductVariantEntity v = new ProductVariantEntity();
        v.setProduct(p);
        v.setEsDefault(true);
        orden = new OrderEntity();
        orden.setIdOrder(10L);
        orden.setTrackingId(UUID.randomUUID());
        orden.setStatus(OrderStatus.COMPLETADA);
        orden.getDetails().add(new OrderDetailEntity(orden, v, 2, new BigDecimal("10000")));
        orden.setDiscount(new BigDecimal("2000"));
        orden.recalcularTotales(); // total 18000

        lenient().when(orderRepository.findById(10L)).thenReturn(Optional.of(orden));
        lenient().when(devolucionRepository.sumMontoDevueltoByOrder(10L)).thenReturn(BigDecimal.ZERO);
        lenient().when(facturaRepository.save(any())).thenAnswer(inv -> {
            FacturaEntity f = inv.getArgument(0);
            f.setIdFactura(99L);
            return f;
        });
        lenient().when(facturaRepository.sumNotasCredito(any())).thenReturn(BigDecimal.ZERO);
    }

    private void arcaAprueba() {
        when(wsfe.solicitarCae(any())).thenReturn(new WsfeClient.ResultadoCae(true, "76123456789012",
                LocalDate.of(2026, 10, 22), List.of(), List.of()));
    }

    @Test
    void facturar_usaSiguienteNumeroYGuardaCae() {
        when(wsfe.ultimoAutorizado(3, 11)).thenReturn(24L);
        arcaAprueba();

        var dto = service.facturar(10L, null, null);

        ArgumentCaptor<WsfeClient.SolicitudCae> req = ArgumentCaptor.forClass(WsfeClient.SolicitudCae.class);
        verify(wsfe).solicitarCae(req.capture());
        assertThat(req.getValue().numero()).isEqualTo(25);
        assertThat(req.getValue().cbteTipo()).isEqualTo(11);
        assertThat(req.getValue().importeTotal()).isEqualByComparingTo("18000");
        assertThat(req.getValue().docTipo()).isEqualTo(99);
        assertThat(req.getValue().condicionIvaReceptorId()).isEqualTo(5);
        assertThat(dto.getNumeroFormateado()).isEqualTo("00003-00000025");
        assertThat(dto.getCae()).isEqualTo("76123456789012");
        assertThat(dto.isPrueba()).isTrue();
    }

    @Test
    void facturar_descuentaDevolucionesPrevias() {
        when(devolucionRepository.sumMontoDevueltoByOrder(10L)).thenReturn(new BigDecimal("9000"));
        when(wsfe.ultimoAutorizado(3, 11)).thenReturn(0L);
        arcaAprueba();

        var dto = service.facturar(10L, null, null);

        assertThat(dto.getImporteTotal()).isEqualByComparingTo("9000");
        assertThat(dto.getItems()).extracting("descripcion").contains("Devoluciones anteriores a la factura");
    }

    @Test
    void facturar_rechazoDeArca_noGuarda() {
        when(wsfe.ultimoAutorizado(3, 11)).thenReturn(0L);
        when(wsfe.solicitarCae(any())).thenReturn(new WsfeClient.ResultadoCae(false, null, null,
                List.of("[10015] Doc invalido"), List.of()));

        assertThatThrownBy(() -> service.facturar(10L, null, null))
                .isInstanceOf(ArcaException.class)
                .hasMessageContaining("10015");
        verify(facturaRepository, never()).save(any());
    }

    @Test
    void facturar_dosVeces_falla() {
        when(facturaRepository.existsByOrder_IdOrderAndTipo(10L, TipoComprobante.FACTURA_C)).thenReturn(true);

        assertThatThrownBy(() -> service.facturar(10L, null, null))
                .isInstanceOf(VentaInvalidaException.class)
                .hasMessageContaining("ya tiene factura");
    }

    @Test
    void facturar_sinConfigurar_dicequeFalta() {
        props.setCertPath(null);

        assertThatThrownBy(() -> service.facturar(10L, null, null))
                .isInstanceOf(VentaInvalidaException.class)
                .hasMessageContaining("ARCA_CERT_PATH");
    }

    @Test
    void facturar_ventaCancelada_falla() {
        orden.setStatus(OrderStatus.CANCELADA);

        assertThatThrownBy(() -> service.facturar(10L, null, null))
                .isInstanceOf(VentaInvalidaException.class)
                .hasMessageContaining("cancelada");
    }

    // ── Receptor ─────────────────────────────────────────────────────────────

    @Test
    void receptor_cuitConDigitoVerificador() {
        assertThat(FacturacionService.cuitValido(20111111112L)).isTrue();
        assertThat(FacturacionService.cuitValido(20111111113L)).isFalse();

        FacturarRequestDTO req = new FacturarRequestDTO();
        req.setDocTipo(80);
        req.setDocNro(20111111113L);
        req.setCondicionIvaReceptorId(6);
        assertThatThrownBy(() -> service.resolverReceptor(req, null, BigDecimal.TEN))
                .hasMessageContaining("CUIT inválido");
    }

    @Test
    void receptor_monotributistaSinCuit_falla() {
        FacturarRequestDTO req = new FacturarRequestDTO();
        req.setCondicionIvaReceptorId(6);

        assertThatThrownBy(() -> service.resolverReceptor(req, null, BigDecimal.TEN))
                .hasMessageContaining("CUIT");
    }

    @Test
    void receptor_consumidorFinalDesdeDiezMillones_exigeDocumento() {
        assertThatThrownBy(() -> service.resolverReceptor(null, null, new BigDecimal("10000000")))
                .hasMessageContaining("DNI o CUIT");
        assertThat(service.resolverReceptor(null, null, new BigDecimal("9999999.99")).docTipo()).isEqualTo(99);
    }

    // ── Nota de crédito ──────────────────────────────────────────────────────

    private FacturaEntity factura() {
        FacturaEntity f = new FacturaEntity();
        f.setIdFactura(50L);
        f.setOrder(orden);
        f.setTipo(TipoComprobante.FACTURA_C);
        f.setPuntoVenta(3);
        f.setNumero(25L);
        f.setFecha(LocalDate.of(2026, 10, 12));
        f.setImporteTotal(new BigDecimal("18000"));
        f.setDocTipo(99);
        f.setDocNro(0L);
        f.setCondicionIvaReceptorId(5);
        f.setCae("76123456789012");
        f.setAmbiente("HOMOLOGACION");
        f.setCreadoEn(LocalDateTime.now().minusHours(1));
        when(facturaRepository.findById(50L)).thenReturn(Optional.of(f));
        return f;
    }

    @Test
    void notaCredito_total_asociaLaFactura() {
        factura();
        when(wsfe.ultimoAutorizado(3, 13)).thenReturn(7L);
        arcaAprueba();

        var dto = service.notaCredito(50L, new NotaCreditoRequestDTO(), null);

        ArgumentCaptor<WsfeClient.SolicitudCae> req = ArgumentCaptor.forClass(WsfeClient.SolicitudCae.class);
        verify(wsfe).solicitarCae(req.capture());
        assertThat(req.getValue().cbteTipo()).isEqualTo(13);
        assertThat(req.getValue().numero()).isEqualTo(8);
        assertThat(req.getValue().importeTotal()).isEqualByComparingTo("18000");
        assertThat(req.getValue().asociado().nro()).isEqualTo(25);
        assertThat(dto.getFacturaAsociadaNumero()).isEqualTo("00003-00000025");
    }

    @Test
    void notaCredito_porDevolucion_usaSuMonto() {
        factura();
        DevolucionEntity d = new DevolucionEntity();
        d.setIdDevolucion(4L);
        d.setOrder(orden);
        d.setFecha(LocalDateTime.now());
        d.setMontoDevuelto(new BigDecimal("9000"));
        when(devolucionRepository.findById(4L)).thenReturn(Optional.of(d));
        when(wsfe.ultimoAutorizado(3, 13)).thenReturn(0L);
        arcaAprueba();
        NotaCreditoRequestDTO req = new NotaCreditoRequestDTO();
        req.setDevolucionId(4L);

        var dto = service.notaCredito(50L, req, null);

        assertThat(dto.getImporteTotal()).isEqualByComparingTo("9000");
        assertThat(dto.getIdDevolucion()).isEqualTo(4L);
    }

    @Test
    void notaCredito_superaSaldo_falla() {
        factura();
        when(facturaRepository.sumNotasCredito(50L)).thenReturn(new BigDecimal("15000"));
        NotaCreditoRequestDTO req = new NotaCreditoRequestDTO();
        req.setImporte(new BigDecimal("5000"));

        assertThatThrownBy(() -> service.notaCredito(50L, req, null))
                .isInstanceOf(VentaInvalidaException.class)
                .hasMessageContaining("supera");
        verify(wsfe, never()).solicitarCae(any());
    }

    @Test
    void notaCredito_devolucionAnteriorALaFactura_falla() {
        factura();
        DevolucionEntity d = new DevolucionEntity();
        d.setIdDevolucion(4L);
        d.setOrder(orden);
        d.setFecha(LocalDateTime.now().minusDays(1));
        d.setMontoDevuelto(new BigDecimal("9000"));
        when(devolucionRepository.findById(4L)).thenReturn(Optional.of(d));
        NotaCreditoRequestDTO req = new NotaCreditoRequestDTO();
        req.setDevolucionId(4L);

        assertThatThrownBy(() -> service.notaCredito(50L, req, null))
                .hasMessageContaining("anterior a la factura");
    }

    // ── QR ───────────────────────────────────────────────────────────────────

    @Test
    void qr_segunEspecificacionDeArca() throws Exception {
        FacturaEntity f = new FacturaEntity();
        f.setTipo(TipoComprobante.FACTURA_C);
        f.setPuntoVenta(3);
        f.setNumero(25L);
        f.setFecha(LocalDate.of(2026, 10, 12));
        f.setImporteTotal(new BigDecimal("18000.00"));
        f.setDocTipo(96);
        f.setDocNro(30123456L);
        f.setCae("76123456789012");

        String url = service.qrUrl(f);

        assertThat(url).startsWith("https://www.arca.gob.ar/fe/qr/?p=");
        String json = new String(Base64.getDecoder().decode(url.substring(url.indexOf("p=") + 2)),
                StandardCharsets.UTF_8);
        Map<?, ?> m = new ObjectMapper().readValue(json, Map.class);
        assertThat(m.get("ver")).isEqualTo(1);
        assertThat(m.get("fecha")).isEqualTo("2026-10-12");
        assertThat(m.get("cuit")).isEqualTo(20111111112L);
        assertThat(m.get("tipoCmp")).isEqualTo(11);
        assertThat(m.get("nroCmp")).isEqualTo(25);
        assertThat(m.get("moneda")).isEqualTo("PES");
        assertThat(m.get("tipoDocRec")).isEqualTo(96);
        assertThat(m.get("tipoCodAut")).isEqualTo("E");
        assertThat(m.get("codAut")).isEqualTo(76123456789012L);
    }
}
