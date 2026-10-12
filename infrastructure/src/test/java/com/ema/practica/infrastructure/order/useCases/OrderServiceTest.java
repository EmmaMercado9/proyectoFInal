package com.ema.practica.infrastructure.order.useCases;

import com.ema.practica.infrastructure.detailOrder.crud.OrderDetailRepository;
import com.ema.practica.infrastructure.detailOrder.rest.model.OrderDetailRequestDTO;
import com.ema.practica.infrastructure.exception.RecursoNoEncontradoException;
import com.ema.practica.infrastructure.exception.StockInsuficienteException;
import com.ema.practica.infrastructure.exception.VentaInvalidaException;
import com.ema.practica.infrastructure.moduloProduct.client.crud.ClientRepository;
import com.ema.practica.infrastructure.moduloProduct.product.entity.ProductEntity;
import com.ema.practica.infrastructure.moduloProduct.productVariant.crud.ProductVariantRepository;
import com.ema.practica.infrastructure.moduloProduct.productVariant.entity.ProductVariantEntity;
import com.ema.practica.infrastructure.order.crud.OrderRepository;
import com.ema.practica.infrastructure.order.entity.OrderEntity;
import com.ema.practica.infrastructure.order.enumOrder.CanalVenta;
import com.ema.practica.infrastructure.order.enumOrder.OrderStatus;
import com.ema.practica.infrastructure.order.enumOrder.PaymentMethod;
import com.ema.practica.infrastructure.order.mapper.OrderMapper;
import com.ema.practica.infrastructure.order.rest.model.OrderRequestDTO;
import com.ema.practica.infrastructure.util.user.crud.IListUserCrud;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock OrderDetailRepository orderDetailRepository;
    @Mock ProductVariantRepository variantRepository;
    @Mock IListUserCrud userRepository;
    @Mock OrderMapper orderMapper;
    @Mock ClientRepository clientRepository;

    @InjectMocks OrderService orderService;

    private ProductVariantEntity variante;

    @BeforeEach
    void setUp() {
        ProductEntity producto = new ProductEntity();
        producto.setPrecioVenta(new BigDecimal("100.00"));

        variante = new ProductVariantEntity();
        variante.setVariantId(1L);
        variante.setProduct(producto);
        variante.setStock(10);
    }

    private OrderRequestDTO request(int cantidad, String descuento) {
        OrderDetailRequestDTO item = new OrderDetailRequestDTO();
        item.setVariantId(1L);
        item.setQuantity(cantidad);

        OrderRequestDTO dto = new OrderRequestDTO();
        dto.setCanal(CanalVenta.ADMIN);
        dto.setPayment(PaymentMethod.values()[0]);
        dto.setItems(List.of(item));
        if (descuento != null) dto.setDiscount(new BigDecimal(descuento));
        return dto;
    }

    @Test
    void crearOrden_descuentaStockYCalculaTotales() {
        when(variantRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(variante));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        orderService.crearOrden(request(3, "50"));

        ArgumentCaptor<OrderEntity> captor = ArgumentCaptor.forClass(OrderEntity.class);
        verify(orderRepository).save(captor.capture());
        OrderEntity orden = captor.getValue();

        assertThat(variante.getStock()).isEqualTo(7);
        assertThat(orden.getSubtotal()).isEqualByComparingTo("300.00");
        assertThat(orden.getTotalAmount()).isEqualByComparingTo("250.00");
        assertThat(orden.getStatus()).isEqualTo(OrderStatus.COMPLETADA);
        assertThat(orden.getUser()).isNull(); // venta de mostrador sin usuario
    }

    @Test
    void crearOrden_sinStockSuficiente_falla() {
        when(variantRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(variante));

        assertThatThrownBy(() -> orderService.crearOrden(request(11, null)))
                .isInstanceOf(StockInsuficienteException.class)
                .hasMessageContaining("disponible 10");

        assertThat(variante.getStock()).isEqualTo(10);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void crearOrden_descuentoMayorAlSubtotal_falla() {
        when(variantRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(variante));

        assertThatThrownBy(() -> orderService.crearOrden(request(1, "150")))
                .isInstanceOf(VentaInvalidaException.class);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void crearOrden_varianteInexistente_falla() {
        when(variantRepository.findByIdForUpdate(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.crearOrden(request(1, null)))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void cancelarOrden_devuelveStock() {
        when(variantRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(variante));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        orderService.crearOrden(request(4, null));
        OrderEntity orden = mockingDetails(orderRepository).getInvocations().stream()
                .filter(i -> i.getMethod().getName().equals("save"))
                .map(i -> (OrderEntity) i.getArgument(0))
                .findFirst().orElseThrow();
        orden.setIdOrder(99L);
        when(orderRepository.findById(99L)).thenReturn(Optional.of(orden));

        orderService.cancelarOrden(99L);

        assertThat(variante.getStock()).isEqualTo(10);
        assertThat(orden.getStatus()).isEqualTo(OrderStatus.CANCELADA);
    }

    @Test
    void cancelarOrden_yaCancelada_falla() {
        OrderEntity orden = new OrderEntity();
        orden.setStatus(OrderStatus.CANCELADA);
        when(orderRepository.findById(5L)).thenReturn(Optional.of(orden));

        assertThatThrownBy(() -> orderService.cancelarOrden(5L))
                .isInstanceOf(VentaInvalidaException.class);
    }

    @Test
    void crearOrden_varianteDesactivada_falla() {
        variante.setStatus(com.ema.practica.infrastructure.util.status.StatusEntity.INACTIVO);
        when(variantRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(variante));

        assertThatThrownBy(() -> orderService.crearOrden(request(1, null)))
                .isInstanceOf(VentaInvalidaException.class)
                .hasMessageContaining("desactivado");
        assertThat(variante.getStock()).isEqualTo(10);
    }
}
