package com.ema.practica.infrastructure.devolucion.useCases;

import com.ema.practica.infrastructure.detailOrder.entity.OrderDetailEntity;
import com.ema.practica.infrastructure.detailOrder.rest.model.OrderDetailRequestDTO;
import com.ema.practica.infrastructure.devolucion.crud.DevolucionRepository;
import com.ema.practica.infrastructure.devolucion.entity.DevolucionEntity;
import com.ema.practica.infrastructure.devolucion.enums.FormaReintegro;
import com.ema.practica.infrastructure.devolucion.enums.TipoDevolucion;
import com.ema.practica.infrastructure.devolucion.mapper.DevolucionMapper;
import com.ema.practica.infrastructure.devolucion.rest.model.DevolucionItemRequestDTO;
import com.ema.practica.infrastructure.devolucion.rest.model.DevolucionRequestDTO;
import com.ema.practica.infrastructure.exception.VentaInvalidaException;
import com.ema.practica.infrastructure.moduloProduct.product.entity.ProductEntity;
import com.ema.practica.infrastructure.moduloProduct.productVariant.crud.ProductVariantRepository;
import com.ema.practica.infrastructure.moduloProduct.productVariant.entity.ProductVariantEntity;
import com.ema.practica.infrastructure.order.crud.OrderRepository;
import com.ema.practica.infrastructure.order.entity.OrderEntity;
import com.ema.practica.infrastructure.order.enumOrder.OrderStatus;
import com.ema.practica.infrastructure.order.enumOrder.PaymentMethod;
import com.ema.practica.infrastructure.order.rest.model.OrderResponseDTO;
import com.ema.practica.infrastructure.order.useCases.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DevolucionServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock ProductVariantRepository variantRepository;
    @Mock DevolucionRepository devolucionRepository;
    @Mock OrderService orderService;
    @Mock DevolucionMapper devolucionMapper;

    @InjectMocks DevolucionService service;

    private ProductVariantEntity variante;
    private OrderEntity orden;
    private OrderDetailEntity detalle;

    // Venta: 2 unidades a $100 con $20 de descuento → cobrado $180
    @BeforeEach
    void setUp() {
        ProductEntity producto = new ProductEntity();
        producto.setNameProduct("Zapatilla");

        variante = new ProductVariantEntity();
        variante.setVariantId(1L);
        variante.setProduct(producto);
        variante.setStock(5);

        orden = new OrderEntity();
        orden.setIdOrder(10L);
        orden.setStatus(OrderStatus.COMPLETADA);
        orden.setPayment(PaymentMethod.EFECTIVO);
        orden.setOrderDate(LocalDateTime.now().minusDays(1));

        detalle = new OrderDetailEntity(orden, variante, 2, new BigDecimal("100.00"));
        detalle.setIdDetail(100L);
        orden.getDetails().add(detalle);
        orden.setDiscount(new BigDecimal("20.00"));
        orden.recalcularTotales();

        lenient().when(orderRepository.findById(10L)).thenReturn(Optional.of(orden));
        lenient().when(variantRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(variante));
        lenient().when(devolucionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(devolucionRepository.sumMontoDevueltoByOrder(10L)).thenReturn(BigDecimal.ZERO);
    }

    private DevolucionRequestDTO devolucion(int cantidad, boolean reingresa) {
        DevolucionItemRequestDTO item = new DevolucionItemRequestDTO();
        item.setIdDetail(100L);
        item.setQuantity(cantidad);
        item.setReingresaStock(reingresa);

        DevolucionRequestDTO dto = new DevolucionRequestDTO();
        dto.setTipo(TipoDevolucion.DEVOLUCION);
        dto.setFormaReintegro(FormaReintegro.EFECTIVO);
        dto.setItems(List.of(item));
        return dto;
    }

    private DevolucionEntity guardada() {
        ArgumentCaptor<DevolucionEntity> captor = ArgumentCaptor.forClass(DevolucionEntity.class);
        verify(devolucionRepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void devolucionParcial_reponeStockYProrrateaDescuento() {
        service.registrar(10L, devolucion(1, true), null);

        DevolucionEntity d = guardada();
        assertThat(variante.getStock()).isEqualTo(6);
        assertThat(detalle.getCantidadDevuelta()).isEqualTo(1);
        assertThat(orden.getStatus()).isEqualTo(OrderStatus.DEVUELTA_PARCIAL);
        assertThat(d.getMontoDevuelto()).isEqualByComparingTo("90.00"); // 100 × 180/200
        assertThat(d.getDiferencia()).isEqualByComparingTo("-90.00");
        assertThat(d.getFormaReintegro()).isEqualTo(FormaReintegro.EFECTIVO);
    }

    @Test
    void devolucionTotal_marcaDevueltaYReintegraTodoLoCobrado() {
        service.registrar(10L, devolucion(2, true), null);

        assertThat(orden.getStatus()).isEqualTo(OrderStatus.DEVUELTA);
        assertThat(guardada().getMontoDevuelto()).isEqualByComparingTo("180.00");
    }

    @Test
    void productoFallado_noVuelveAlStock() {
        service.registrar(10L, devolucion(1, false), null);

        assertThat(variante.getStock()).isEqualTo(5);
        verify(variantRepository, never()).findByIdForUpdate(any());
        assertThat(guardada().getItems().get(0).getReingresaStock()).isFalse();
    }

    @Test
    void masUnidadesQueLasVendidas_falla() {
        assertThatThrownBy(() -> service.registrar(10L, devolucion(3, true), null))
                .isInstanceOf(VentaInvalidaException.class)
                .hasMessageContaining("como máximo 2");
        assertThat(variante.getStock()).isEqualTo(5);
    }

    @Test
    void fueraDePlazo_falla() {
        orden.setOrderDate(LocalDateTime.now().minusDays(4));

        assertThatThrownBy(() -> service.registrar(10L, devolucion(1, true), null))
                .isInstanceOf(VentaInvalidaException.class)
                .hasMessageContaining("plazo");
    }

    @Test
    void ventaCancelada_falla() {
        orden.setStatus(OrderStatus.CANCELADA);

        assertThatThrownBy(() -> service.registrar(10L, devolucion(1, true), null))
                .isInstanceOf(VentaInvalidaException.class)
                .hasMessageContaining("cancelada");
    }

    @Test
    void devolucionSinFormaDeReintegro_falla() {
        DevolucionRequestDTO dto = devolucion(1, true);
        dto.setFormaReintegro(null);

        assertThatThrownBy(() -> service.registrar(10L, dto, null))
                .isInstanceOf(VentaInvalidaException.class)
                .hasMessageContaining("reintegra");
    }

    @Test
    void cambio_creaVentaNuevaYCalculaDiferencia() {
        OrderEntity nueva = new OrderEntity();
        nueva.setIdOrder(11L);
        nueva.setTotalAmount(new BigDecimal("150.00"));
        OrderResponseDTO creada = new OrderResponseDTO();
        creada.setIdOrder(11L);
        when(orderService.crearOrden(any())).thenReturn(creada);
        when(orderRepository.findById(11L)).thenReturn(Optional.of(nueva));

        OrderDetailRequestDTO nuevoItem = new OrderDetailRequestDTO();
        nuevoItem.setVariantId(2L);
        nuevoItem.setQuantity(1);
        DevolucionRequestDTO dto = devolucion(1, true);
        dto.setTipo(TipoDevolucion.CAMBIO);
        dto.setFormaReintegro(null);
        dto.setPaymentDiferencia(PaymentMethod.TARJETA_DEBITO);
        dto.setItemsNuevos(List.of(nuevoItem));

        service.registrar(10L, dto, null);

        DevolucionEntity d = guardada();
        assertThat(d.getOrdenCambio()).isSameAs(nueva);
        assertThat(d.getMontoNuevaVenta()).isEqualByComparingTo("150.00");
        assertThat(d.getDiferencia()).isEqualByComparingTo("60.00"); // paga el cliente
        assertThat(d.getFormaReintegro()).isNull();
        verify(orderService).crearOrden(argThat(o ->
                o.getPayment() == PaymentMethod.TARJETA_DEBITO
                        && o.getDescription().contains("#10")));
    }

    @Test
    void cambioSinProductosNuevos_falla() {
        DevolucionRequestDTO dto = devolucion(1, true);
        dto.setTipo(TipoDevolucion.CAMBIO);

        assertThatThrownBy(() -> service.registrar(10L, dto, null))
                .isInstanceOf(VentaInvalidaException.class)
                .hasMessageContaining("producto nuevo");
    }
}
