package com.ema.practica.infrastructure.order.useCases;

import com.ema.practica.infrastructure.detailOrder.crud.OrderDetailRepository;
import com.ema.practica.infrastructure.detailOrder.entity.OrderDetailEntity;
import com.ema.practica.infrastructure.detailOrder.rest.model.OrderDetailRequestDTO;
import com.ema.practica.infrastructure.exception.RecursoNoEncontradoException;

import com.ema.practica.infrastructure.exception.StockInsuficienteException;
import com.ema.practica.infrastructure.exception.VentaInvalidaException;
import com.ema.practica.infrastructure.moduloProduct.client.crud.ClientRepository;
import com.ema.practica.infrastructure.moduloProduct.client.entity.ClientEntity;
import com.ema.practica.infrastructure.moduloProduct.productVariant.crud.ProductVariantRepository;
import com.ema.practica.infrastructure.moduloProduct.productVariant.entity.ProductVariantEntity;
import com.ema.practica.infrastructure.order.crud.OrderRepository;
import com.ema.practica.infrastructure.order.entity.OrderEntity;
import com.ema.practica.infrastructure.order.enumOrder.OrderStatus;
import com.ema.practica.infrastructure.order.mapper.OrderMapper;
import com.ema.practica.infrastructure.order.rest.model.OrderListadoDTO;
import com.ema.practica.infrastructure.order.rest.model.OrderRequestDTO;
import com.ema.practica.infrastructure.order.rest.model.OrderResponseDTO;
import com.ema.practica.infrastructure.util.user.crud.IListUserCrud;
import com.ema.practica.infrastructure.util.user.entity.UserEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.ema.practica.infrastructure.util.status.StatusEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductVariantRepository variantRepository;
    private final IListUserCrud userRepository;
    private final OrderMapper orderMapper;
    private final ClientRepository clientRepository;


    @Transactional
    public OrderResponseDTO crearOrden(OrderRequestDTO dto) {

        // 1. Resolver usuario — puede ser anónimo en el mostrador
        UserEntity user = null;
        if (dto.getUserId() != null) {
            user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Usuario no encontrado con ID: " + dto.getUserId()
                            ));
                    ;}

        OrderEntity order = new OrderEntity();

        if (dto.getClienteId() != null) {
            ClientEntity cliente = clientRepository.findById(dto.getClienteId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Cliente", dto.getClienteId()));
            order.setClientEntity(cliente);
        }

        order.setUser(user);
        order.setCanal(dto.getCanal());
        order.setPayment(dto.getPayment());
        order.setStatus(OrderStatus.COMPLETADA);
        order.setOrderDate(LocalDateTime.now());
        order.setAddress(dto.getAddress());
        order.setDescription(dto.getDescription());
        order.setDiscount(dto.getDiscount() != null
                ? dto.getDiscount() : BigDecimal.ZERO);

        // 3. Procesar cada item del carrito
        for (OrderDetailRequestDTO item : dto.getItems()) {

            // Bloqueo pesimista — evita que dos ventas simultáneas
            // vendan el mismo par
            ProductVariantEntity variant = variantRepository
                    .findByIdForUpdate(item.getVariantId())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Variante", item.getVariantId()));

            // No se venden variantes ni productos desactivados
            if (variant.getStatus() == StatusEntity.INACTIVO
                    || variant.getProduct().getStatus() == StatusEntity.INACTIVO) {
                throw new VentaInvalidaException("\"" + variant.getProduct().getNameProduct()
                        + "\" (" + variant.getSku() + ") está desactivado y no se puede vender");
            }

            // Validar stock
            if (!variant.tieneStockSuficiente(item.getQuantity())) {
                throw new StockInsuficienteException(
                        variant.getVariantId(),
                        variant.getStock(),
                        item.getQuantity()
                );
            }

            // Precio al momento de la venta — se congela aquí
            BigDecimal precioUnitario = variant.getPrecioEfectivo();

            // Descontar stock
            variant.setStock(variant.getStock() - item.getQuantity());

            // Crear el detalle
            OrderDetailEntity detail = new OrderDetailEntity(
                    order, variant, item.getQuantity(), precioUnitario
            );
            order.getDetails().add(detail);
        }

        // 4. Calcular totales desde los detalles
        order.recalcularTotales();

        // 5. Guardar — cascade guarda los detalles automáticamente
        OrderEntity guardada = orderRepository.save(order);

        return orderMapper.toResponseDTO(guardada);
    }

    // Listado para admin — ve todas las órdenes
    public Page<OrderListadoDTO> listarTodas(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(orderMapper::toListadoDTO);
    }

    // Listado para cliente online — solo las propias
    public Page<OrderListadoDTO> listarPorUsuario(UUID userId, Pageable pageable) {
        return orderRepository.findByUser_IdUser(userId, pageable)
                .map(orderMapper::toListadoDTO);
    }

    // Detalle por trackingId — se muestra al cliente
    public OrderResponseDTO getByTrackingId(UUID trackingId) {
        OrderEntity order = orderRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden", trackingId));
        return orderMapper.toResponseDTO(order);
    }

    // Cancelar orden — devuelve el stock
    @Transactional
    public OrderResponseDTO cancelarOrden(Long idOrder) throws VentaInvalidaException {
        return cancelarOrden(idOrder, null, null);
    }

    @Transactional
    public OrderResponseDTO cancelarOrden(Long idOrder, String motivo, String usuario)
            throws VentaInvalidaException {
        OrderEntity order = orderRepository.findById(idOrder)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Orden", idOrder));

        if (order.getStatus() != OrderStatus.COMPLETADA) {
            throw new VentaInvalidaException(order.getStatus() == OrderStatus.DEVUELTA_PARCIAL
                    ? "La venta tiene devoluciones: devolvé el resto de los productos en lugar de cancelarla"
                    : "Solo se pueden cancelar órdenes completadas");
        }

        // Devolver stock de cada variante
        for (OrderDetailEntity detail : order.getDetails()) {
            ProductVariantEntity variant = detail.getVariant();
            variant.setStock(variant.getStock() + detail.getQuantity());
        }

        order.setStatus(OrderStatus.CANCELADA);
        order.setMotivoCancelacion(motivo != null && !motivo.isBlank() ? motivo.trim() : null);
        order.setFechaCancelacion(LocalDateTime.now());
        order.setCanceladoPor(usuario);
        return orderMapper.toResponseDTO(orderRepository.save(order));
    }
}