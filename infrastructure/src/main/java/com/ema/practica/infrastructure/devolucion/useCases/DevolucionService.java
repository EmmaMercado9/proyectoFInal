package com.ema.practica.infrastructure.devolucion.useCases;

import com.ema.practica.infrastructure.detailOrder.entity.OrderDetailEntity;
import com.ema.practica.infrastructure.devolucion.crud.DevolucionRepository;
import com.ema.practica.infrastructure.devolucion.entity.DevolucionDetalleEntity;
import com.ema.practica.infrastructure.devolucion.entity.DevolucionEntity;
import com.ema.practica.infrastructure.devolucion.enums.TipoDevolucion;
import com.ema.practica.infrastructure.devolucion.mapper.DevolucionMapper;
import com.ema.practica.infrastructure.devolucion.rest.model.DevolucionItemRequestDTO;
import com.ema.practica.infrastructure.devolucion.rest.model.DevolucionRequestDTO;
import com.ema.practica.infrastructure.devolucion.rest.model.DevolucionResponseDTO;
import com.ema.practica.infrastructure.exception.RecursoNoEncontradoException;
import com.ema.practica.infrastructure.exception.VentaInvalidaException;
import com.ema.practica.infrastructure.moduloProduct.productVariant.crud.ProductVariantRepository;
import com.ema.practica.infrastructure.moduloProduct.productVariant.entity.ProductVariantEntity;
import com.ema.practica.infrastructure.order.crud.OrderRepository;
import com.ema.practica.infrastructure.order.entity.OrderEntity;
import com.ema.practica.infrastructure.order.enumOrder.CanalVenta;
import com.ema.practica.infrastructure.order.enumOrder.OrderStatus;
import com.ema.practica.infrastructure.order.rest.model.OrderRequestDTO;
import com.ema.practica.infrastructure.order.rest.model.OrderResponseDTO;
import com.ema.practica.infrastructure.order.useCases.OrderService;
import com.ema.practica.infrastructure.util.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Devoluciones y cambios sobre ventas completadas.
 *
 * - DEVOLUCION: se devuelven ítems (total o parcialmente) y se reintegra su valor.
 * - CAMBIO: igual, pero además se crea una venta nueva con lo que se lleva el
 *   cliente; se registra la diferencia (paga él o se le reintegra).
 *
 * El valor de cada ítem devuelto es su precio congelado en la venta, con el
 * descuento de la venta prorrateado (total / subtotal).
 */
@Service
@RequiredArgsConstructor
public class DevolucionService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final OrderRepository orderRepository;
    private final ProductVariantRepository variantRepository;
    private final DevolucionRepository devolucionRepository;
    private final OrderService orderService;
    private final DevolucionMapper devolucionMapper;

    @Value("${app.devoluciones.plazo-dias:3}")
    private int plazoDias = 3;

    @Transactional
    public DevolucionResponseDTO registrar(Long idOrder, DevolucionRequestDTO dto, UserEntity usuario) {
        OrderEntity order = orderRepository.findById(idOrder)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden", idOrder));

        validarOrden(order);

        DevolucionEntity devolucion = new DevolucionEntity();
        devolucion.setOrder(order);
        devolucion.setTipo(dto.getTipo());
        devolucion.setUsuario(usuario);
        devolucion.setFecha(LocalDateTime.now());
        devolucion.setMotivo(dto.getMotivo() != null && !dto.getMotivo().isBlank()
                ? dto.getMotivo().trim() : null);

        // 1. Ítems devueltos: validar cantidades, reponer stock y valorizar
        BigDecimal montoDevuelto = procesarItems(order, dto.getItems(), devolucion);

        // 2. Estado de la venta original
        boolean todoDevuelto = order.getDetails().stream().allMatch(d -> d.getCantidadDevolvible() == 0);
        order.setStatus(todoDevuelto ? OrderStatus.DEVUELTA : OrderStatus.DEVUELTA_PARCIAL);

        // Si se devolvió todo, el total reintegrado tiene que dar exacto el total
        // cobrado (evita diferencias de centavos por el prorrateo del descuento)
        if (todoDevuelto) {
            BigDecimal previo = devolucionRepository.sumMontoDevueltoByOrder(idOrder);
            montoDevuelto = order.getTotalAmount().subtract(previo);
        }
        devolucion.setMontoDevuelto(montoDevuelto);

        // 3. Cambio: venta nueva con lo que se lleva
        BigDecimal montoNuevaVenta = BigDecimal.ZERO;
        boolean hayNuevos = dto.getItemsNuevos() != null && !dto.getItemsNuevos().isEmpty();
        if (dto.getTipo() == TipoDevolucion.CAMBIO) {
            if (!hayNuevos) {
                throw new VentaInvalidaException("Un cambio necesita al menos un producto nuevo");
            }
            OrderEntity nueva = crearVentaCambio(order, dto, usuario);
            devolucion.setOrdenCambio(nueva);
            montoNuevaVenta = nueva.getTotalAmount();
        } else if (hayNuevos) {
            throw new VentaInvalidaException("Para llevarse otros productos registrá un CAMBIO");
        }
        devolucion.setMontoNuevaVenta(montoNuevaVenta);

        // 4. Diferencia: > 0 paga el cliente, < 0 se le reintegra
        BigDecimal diferencia = montoNuevaVenta.subtract(montoDevuelto);
        devolucion.setDiferencia(diferencia);
        if (diferencia.signum() < 0) {
            if (dto.getFormaReintegro() == null) {
                throw new VentaInvalidaException("Indicá cómo se reintegra el dinero al cliente");
            }
            devolucion.setFormaReintegro(dto.getFormaReintegro());
        }

        return devolucionMapper.toDTO(devolucionRepository.save(devolucion));
    }

    @Transactional(readOnly = true)
    public List<DevolucionResponseDTO> listarPorOrden(Long idOrder) {
        if (!orderRepository.existsById(idOrder)) {
            throw new RecursoNoEncontradoException("Orden", idOrder);
        }
        return devolucionRepository.findByOrder_IdOrderOrderByFechaDesc(idOrder).stream()
                .map(devolucionMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<DevolucionResponseDTO> listar(Pageable pageable) {
        return devolucionRepository.findAllByOrderByFechaDesc(pageable).map(devolucionMapper::toDTO);
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private void validarOrden(OrderEntity order) {
        if (order.getStatus() != OrderStatus.COMPLETADA
                && order.getStatus() != OrderStatus.DEVUELTA_PARCIAL) {
            throw new VentaInvalidaException(switch (order.getStatus()) {
                case CANCELADA -> "La venta está cancelada";
                case DEVUELTA -> "Ya se devolvieron todos los productos de esta venta";
                default -> "Solo se pueden devolver productos de ventas completadas";
            });
        }
        LocalDateTime limite = order.getOrderDate().plusDays(plazoDias);
        if (LocalDateTime.now().isAfter(limite)) {
            throw new VentaInvalidaException("Venció el plazo de " + plazoDias
                    + " días para devoluciones y cambios (era hasta el " + limite.format(FMT) + ")");
        }
    }

    private BigDecimal procesarItems(OrderEntity order, List<DevolucionItemRequestDTO> items,
                                     DevolucionEntity devolucion) {
        Map<Long, OrderDetailEntity> detalles = order.getDetails().stream()
                .collect(Collectors.toMap(OrderDetailEntity::getIdDetail, Function.identity()));
        BigDecimal factor = factorDescuento(order);
        Set<Long> vistos = new HashSet<>();
        BigDecimal total = BigDecimal.ZERO;

        for (DevolucionItemRequestDTO item : items) {
            OrderDetailEntity detalle = detalles.get(item.getIdDetail());
            if (detalle == null) {
                throw new VentaInvalidaException("El ítem " + item.getIdDetail() + " no pertenece a esta venta");
            }
            if (!vistos.add(item.getIdDetail())) {
                throw new VentaInvalidaException("El ítem " + item.getIdDetail() + " está repetido");
            }
            String nombre = detalle.getVariant().getProduct().getNameProduct();
            int disponible = detalle.getCantidadDevolvible();
            if (item.getQuantity() > disponible) {
                throw new VentaInvalidaException(disponible == 0
                        ? "\"" + nombre + "\" ya fue devuelto por completo"
                        : "De \"" + nombre + "\" se pueden devolver como máximo " + disponible);
            }

            boolean reingresa = !Boolean.FALSE.equals(item.getReingresaStock());
            if (reingresa) {
                ProductVariantEntity variante = variantRepository
                        .findByIdForUpdate(detalle.getVariant().getVariantId())
                        .orElseThrow(() -> new RecursoNoEncontradoException(
                                "Variante", detalle.getVariant().getVariantId()));
                variante.setStock(variante.getStock() + item.getQuantity());
            }
            detalle.setCantidadDevuelta(detalle.getCantidadDevueltaSegura() + item.getQuantity());

            BigDecimal monto = detalle.getPriceUnit()
                    .multiply(BigDecimal.valueOf(item.getQuantity()))
                    .multiply(factor)
                    .setScale(2, RoundingMode.HALF_UP);
            devolucion.getItems().add(new DevolucionDetalleEntity(
                    devolucion, detalle, item.getQuantity(), reingresa, monto));
            total = total.add(monto);
        }
        return total;
    }

    // Proporción efectivamente cobrada (aplica el descuento de la venta a cada ítem)
    private BigDecimal factorDescuento(OrderEntity order) {
        if (order.getSubtotal() == null || order.getSubtotal().signum() == 0) return BigDecimal.ONE;
        return order.getTotalAmount().divide(order.getSubtotal(), 6, RoundingMode.HALF_UP);
    }

    private OrderEntity crearVentaCambio(OrderEntity original, DevolucionRequestDTO dto, UserEntity usuario) {
        OrderRequestDTO nueva = new OrderRequestDTO();
        nueva.setCanal(CanalVenta.ADMIN);
        nueva.setUserId(usuario != null ? usuario.getIdUser() : null);
        nueva.setClienteId(original.getClientEntity() != null
                ? original.getClientEntity().getIdClient() : null);
        nueva.setPayment(dto.getPaymentDiferencia() != null
                ? dto.getPaymentDiferencia() : original.getPayment());
        nueva.setDescription("Cambio de la venta #" + original.getIdOrder());
        nueva.setItems(dto.getItemsNuevos());

        OrderResponseDTO creada = orderService.crearOrden(nueva);
        return orderRepository.findById(creada.getIdOrder())
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden", creada.getIdOrder()));
    }
}
