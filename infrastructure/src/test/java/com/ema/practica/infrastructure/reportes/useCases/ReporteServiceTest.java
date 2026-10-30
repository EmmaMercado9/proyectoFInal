package com.ema.practica.infrastructure.reportes.useCases;

import com.ema.practica.infrastructure.detailOrder.entity.OrderDetailEntity;
import com.ema.practica.infrastructure.devolucion.crud.DevolucionRepository;
import com.ema.practica.infrastructure.devolucion.entity.DevolucionDetalleEntity;
import com.ema.practica.infrastructure.devolucion.entity.DevolucionEntity;
import com.ema.practica.infrastructure.exception.VentaInvalidaException;
import com.ema.practica.infrastructure.facturacion.config.ArcaProperties;
import com.ema.practica.infrastructure.facturacion.crud.FacturaRepository;
import com.ema.practica.infrastructure.importacion.crud.FacturaImportadaRepository;
import com.ema.practica.infrastructure.moduloProduct.product.entity.ProductEntity;
import com.ema.practica.infrastructure.moduloProduct.productVariant.crud.ProductVariantRepository;
import com.ema.practica.infrastructure.moduloProduct.productVariant.entity.ProductVariantEntity;
import com.ema.practica.infrastructure.order.crud.OrderRepository;
import com.ema.practica.infrastructure.order.entity.OrderEntity;
import com.ema.practica.infrastructure.order.enumOrder.OrderStatus;
import com.ema.practica.infrastructure.order.enumOrder.PaymentMethod;
import com.ema.practica.infrastructure.reportes.rest.model.ReporteDTO;
import com.ema.practica.infrastructure.util.status.StatusEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReporteServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock DevolucionRepository devolucionRepository;
    @Mock ProductVariantRepository variantRepository;
    @Mock FacturaRepository facturaRepository;
    @Mock FacturaImportadaRepository facturaImportadaRepository;
    @Mock ArcaProperties arcaProperties;

    ReporteService service;

    static final LocalDate HOY = LocalDate.of(2026, 10, 30);
    long ids = 1;
    final List<OrderEntity> ordenes = new ArrayList<>();
    final List<DevolucionEntity> devoluciones = new ArrayList<>();
    final List<ProductVariantEntity> variantes = new ArrayList<>();

    @BeforeEach
    void setUp() {
        service = new ReporteService(orderRepository, devolucionRepository, variantRepository,
                facturaRepository, facturaImportadaRepository, arcaProperties);
        when(arcaProperties.getAmbiente()).thenReturn(ArcaProperties.Ambiente.HOMOLOGACION);
        // El repositorio devuelve las órdenes cuya fecha cae en el rango pedido
        when(orderRepository.findByFechaBetween(any(), any())).thenAnswer(inv -> {
            LocalDateTime desde = inv.getArgument(0), hasta = inv.getArgument(1);
            return ordenes.stream().filter(o -> !o.getOrderDate().isBefore(desde) && !o.getOrderDate().isAfter(hasta)).toList();
        });
        when(devolucionRepository.findByOrder_IdOrderIn(anyCollection())).thenAnswer(inv -> {
            java.util.Collection<Long> idsOrden = inv.getArgument(0);
            return devoluciones.stream().filter(d -> idsOrden.contains(d.getOrder().getIdOrder())).toList();
        });
        when(variantRepository.findAll()).thenReturn(variantes);
        when(facturaRepository.findByFechaBetweenAndAmbiente(any(), any(), anyString())).thenReturn(List.of());
        when(facturaImportadaRepository.findByFechaBetween(any(), any())).thenReturn(List.of());
    }

    private ProductVariantEntity variante(String nombre, String precio, String costo, int stock, int minimo) {
        ProductEntity p = new ProductEntity();
        p.setIdProduct(UUID.randomUUID());
        p.setNameProduct(nombre);
        p.setSku(nombre.toUpperCase());
        p.setStatus(StatusEntity.ACTIVO);
        p.setTieneVariantes(true);
        ProductVariantEntity v = new ProductVariantEntity();
        v.setVariantId(ids++);
        v.setSku(nombre + "-" + v.getVariantId());
        v.setPrecioVenta(new BigDecimal(precio));
        v.setCosto(new BigDecimal(costo));
        v.setStock(stock);
        v.setStockMinimo(minimo);
        v.setEsDefault(false);
        v.setStatus(StatusEntity.ACTIVO);
        p.agregarVariante(v);
        variantes.add(v);
        return v;
    }

    private OrderEntity venta(LocalDateTime fecha, PaymentMethod pago, String descuento, Object... varianteYCantidad) {
        OrderEntity o = new OrderEntity();
        o.setIdOrder(ids++);
        o.setOrderDate(fecha);
        o.setPayment(pago);
        o.setStatus(OrderStatus.COMPLETADA);
        o.setDiscount(new BigDecimal(descuento));
        for (int i = 0; i < varianteYCantidad.length; i += 2) {
            ProductVariantEntity v = (ProductVariantEntity) varianteYCantidad[i];
            o.getDetails().add(new OrderDetailEntity(o, v, (Integer) varianteYCantidad[i + 1], v.getPrecioEfectivo()));
        }
        o.recalcularTotales();
        ordenes.add(o);
        return o;
    }

    private void devolver(OrderEntity o, OrderDetailEntity d, int cantidad, boolean reingresa, String monto) {
        DevolucionEntity dev = new DevolucionEntity();
        dev.setOrder(o);
        dev.setMontoDevuelto(new BigDecimal(monto));
        dev.getItems().add(new DevolucionDetalleEntity(dev, d, cantidad, reingresa, new BigDecimal(monto)));
        d.setCantidadDevuelta(d.getCantidadDevueltaSegura() + cantidad);
        o.setStatus(OrderStatus.DEVUELTA_PARCIAL);
        devoluciones.add(dev);
    }

    @Test
    void resumen_calculaVentasNetasGananciaYMargen() {
        ProductVariantEntity remera = variante("Remera", "10000", "6000", 20, 2);
        ProductVariantEntity zapa = variante("Zapatilla", "100000", "70000", 20, 2);
        venta(HOY.atTime(10, 0), PaymentMethod.EFECTIVO, "0", remera, 2);            // 20.000, costo 12.000
        venta(HOY.atTime(18, 0), PaymentMethod.TARJETA_DEBITO, "10000", zapa, 1);     // 90.000, costo 70.000

        ReporteDTO.Resumen r = service.generar(HOY, HOY, HOY).actual();

        assertThat(r.ventasBrutas()).isEqualByComparingTo("120000");
        assertThat(r.descuentos()).isEqualByComparingTo("10000");
        assertThat(r.ventasNetas()).isEqualByComparingTo("110000");
        assertThat(r.costoMercaderia()).isEqualByComparingTo("82000");
        assertThat(r.gananciaBruta()).isEqualByComparingTo("28000");
        assertThat(r.margen()).isEqualTo(25.5);
        assertThat(r.cantidadVentas()).isEqualTo(2);
        assertThat(r.unidades()).isEqualTo(3);
        assertThat(r.ticketPromedio()).isEqualByComparingTo("55000");
    }

    @Test
    void devolucionDeProductoFallado_descuentaVentaYSumaPerdida() {
        ProductVariantEntity remera = variante("Remera", "10000", "6000", 20, 2);
        OrderEntity o = venta(HOY.atTime(10, 0), PaymentMethod.EFECTIVO, "0", remera, 3);   // 30.000
        devolver(o, o.getDetails().get(0), 1, false, "10000");                               // 1 fallada

        ReporteDTO r = service.generar(HOY, HOY, HOY);

        assertThat(r.actual().ventasNetas()).isEqualByComparingTo("20000");
        assertThat(r.actual().costoMercaderia()).isEqualByComparingTo("12000");    // 2 netas × 6.000
        assertThat(r.actual().perdidaFallados()).isEqualByComparingTo("6000");     // la fallada no vuelve al stock
        assertThat(r.actual().gananciaBruta()).isEqualByComparingTo("2000");
        assertThat(r.perdidas().unidadesFalladas()).isEqualTo(1);
    }

    @Test
    void ventaCancelada_noCuentaPeroSeInforma() {
        ProductVariantEntity remera = variante("Remera", "10000", "6000", 20, 2);
        venta(HOY.atTime(10, 0), PaymentMethod.EFECTIVO, "0", remera, 1).setStatus(OrderStatus.CANCELADA);

        ReporteDTO.Resumen r = service.generar(HOY, HOY, HOY).actual();

        assertThat(r.cantidadVentas()).isZero();
        assertThat(r.ventasNetas()).isEqualByComparingTo("0");
        assertThat(r.canceladas()).isEqualTo(1);
        assertThat(r.montoCancelado()).isEqualByComparingTo("10000");
    }

    @Test
    void ventaPorDebajoDelCosto_apareceEnPerdidas() {
        ProductVariantEntity liquidacion = variante("Buzo", "5000", "8000", 20, 2);
        venta(HOY.atTime(10, 0), PaymentMethod.EFECTIVO, "0", liquidacion, 2);

        ReporteDTO r = service.generar(HOY, HOY, HOY);

        assertThat(r.perdidas().perdidaVentasBajoCosto()).isEqualByComparingTo("6000");
        assertThat(r.perdidas().vendidosBajoCosto()).extracting("producto").containsExactly("Buzo");
    }

    @Test
    void ventaVieja_sinCostoGuardado_usaElCostoActualYLoAvisa() {
        ProductVariantEntity remera = variante("Remera", "10000", "6000", 20, 2);
        OrderEntity o = venta(HOY.atTime(10, 0), PaymentMethod.EFECTIVO, "0", remera, 1);
        o.getDetails().get(0).setCostoUnitario(null);

        ReporteDTO r = service.generar(HOY, HOY, HOY);

        assertThat(r.actual().costoMercaderia()).isEqualByComparingTo("6000");
        assertThat(r.detallesConCostoEstimado()).isEqualTo(1);
        assertThat(r.observaciones()).anyMatch(s -> s.contains("costo actual"));
    }

    @Test
    void comparaConElPeriodoAnterior() {
        ProductVariantEntity remera = variante("Remera", "10000", "6000", 20, 2);
        venta(HOY.minusDays(1).atTime(10, 0), PaymentMethod.EFECTIVO, "0", remera, 1);   // período anterior
        venta(HOY.atTime(10, 0), PaymentMethod.EFECTIVO, "0", remera, 2);

        ReporteDTO r = service.generar(HOY, HOY, HOY);

        assertThat(r.anterior().ventasNetas()).isEqualByComparingTo("10000");
        assertThat(r.observaciones()).anyMatch(s -> s.contains("subieron 100,0 %"));
    }

    @Test
    void recomienda_reponerLoQueSeVende_yPromocionarLoQueNoRota() {
        ProductVariantEntity sinStock = variante("Runfalcon", "120000", "84000", 0, 1);
        ProductVariantEntity pocoStock = variante("Galaxy", "126000", "84000", 2, 1);
        ProductVariantEntity quieta = variante("Botin", "130000", "91000", 5, 1);
        for (int i = 0; i < 10; i++) {
            venta(HOY.minusDays(i).atTime(12, 0), PaymentMethod.EFECTIVO, "0", pocoStock, 1);
        }
        venta(HOY.minusDays(3).atTime(12, 0), PaymentMethod.EFECTIVO, "0", sinStock, 1);

        ReporteDTO.Recomendaciones rec = service.generar(HOY.minusDays(29), HOY, HOY).recomendaciones();

        // Primero lo urgente (sin stock y con ventas)
        assertThat(rec.reponer().get(0).producto()).isEqualTo("Runfalcon");
        assertThat(rec.reponer().get(0).urgente()).isTrue();
        // Galaxy: 10 vendidas en 30 días, quedan 2 (6 días de cobertura) → comprar 10 − 2 = 8
        ReporteDTO.Reposicion galaxy = rec.reponer().get(1);
        assertThat(galaxy.diasCobertura()).isEqualTo(6.0);
        assertThat(galaxy.sugerido()).isEqualTo(8);
        assertThat(galaxy.costoEstimado()).isEqualByComparingTo("672000");
        // Botín: 5 en stock sin ventas en 90 días
        assertThat(rec.sinRotacion()).extracting("producto").containsExactly("Botin");
        assertThat(rec.sinRotacion().get(0).capitalInmovilizado()).isEqualByComparingTo("455000");
    }

    @Test
    void noRecomiendaComprarSiElStockAlcanzaAunqueEsteBajoElMinimo() {
        ProductVariantEntity lenta = variante("Runfalcon", "120000", "84000", 5, 5);   // 5 en stock, mínimo 5
        venta(HOY.minusDays(2).atTime(12, 0), PaymentMethod.EFECTIVO, "0", lenta, 1);   // 1 venta en 30 días

        ReporteDTO.Recomendaciones rec = service.generar(HOY.minusDays(29), HOY, HOY).recomendaciones();

        assertThat(rec.reponer()).isEmpty();   // 5 unidades alcanzan para 150 días
    }

    @Test
    void inventario_valorizaElStock() {
        variante("Remera", "10000", "6000", 3, 2);   // bajo mínimo (3 > 2? no) → normal
        variante("Buzo", "20000", "12000", 0, 2);    // sin stock
        variante("Short", "8000", "5000", 1, 2);     // bajo mínimo

        ReporteDTO.Inventario inv = service.generar(HOY, HOY, HOY).inventario();

        assertThat(inv.unidades()).isEqualTo(4);
        assertThat(inv.valorAlCosto()).isEqualByComparingTo("23000");
        assertThat(inv.valorAlPrecio()).isEqualByComparingTo("38000");
        assertThat(inv.variantesSinStock()).isEqualTo(1);
        assertThat(inv.variantesBajoMinimo()).isEqualTo(1);
    }

    @Test
    void rangoInvalido_falla() {
        assertThatThrownBy(() -> service.generar(HOY, HOY.minusDays(1), HOY))
                .isInstanceOf(VentaInvalidaException.class);
    }
}
