package com.ema.practica.infrastructure.order.rest.api;

import com.ema.practica.infrastructure.exception.VentaInvalidaException;
import com.ema.practica.infrastructure.order.enumOrder.CanalVenta;
import com.ema.practica.infrastructure.order.rest.model.CancelarOrdenDTO;
import com.ema.practica.infrastructure.order.rest.model.OrderListadoDTO;
import com.ema.practica.infrastructure.order.rest.model.OrderRequestDTO;
import com.ema.practica.infrastructure.order.rest.model.OrderResponseDTO;
import com.ema.practica.infrastructure.order.useCases.OrderService;
import com.ema.practica.infrastructure.util.user.entity.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ── Punto de venta (admin) ───────────────────────────────────────────────

    @PostMapping("/admin/orders")
    public ResponseEntity<OrderResponseDTO> crearOrden(
            @RequestBody @Valid OrderRequestDTO dto) {
        dto.setCanal(CanalVenta.ADMIN);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.crearOrden(dto));
    }

    @GetMapping("/admin/orders")
    public ResponseEntity<Page<OrderListadoDTO>> listarOrdenes(Pageable pageable) {
        return ResponseEntity.ok(orderService.listarTodas(pageable));
    }

    // Body opcional: { "motivo": "..." }
    @PatchMapping("/admin/orders/{id}/cancelar")
    public ResponseEntity<OrderResponseDTO> cancelar(
            @PathVariable Long id,
            @RequestBody(required = false) @Valid CancelarOrdenDTO body,
            @AuthenticationPrincipal SecurityUser securityUser) throws VentaInvalidaException {
        String motivo = body != null ? body.getMotivo() : null;
        String usuario = securityUser != null ? securityUser.getUserEntity().getName() : null;
        return ResponseEntity.ok(orderService.cancelarOrden(id, motivo, usuario));
    }

    // ── Tienda online (cliente) ──────────────────────────────────────────────

    @PostMapping("/tienda/orders")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<OrderResponseDTO> crearOrdenOnline(
            @RequestBody @Valid OrderRequestDTO dto,
            @AuthenticationPrincipal SecurityUser securityUser) {

        dto.setCanal(CanalVenta.ONLINE);

        // El cliente solo puede comprar a su propio nombre
        dto.setUserId(securityUser.getUserEntity().getIdUser());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.crearOrden(dto));
    }

    @GetMapping("/tienda/mis-ordenes")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<Page<OrderListadoDTO>> misOrdenes(
            @AuthenticationPrincipal SecurityUser securityUser,
            Pageable pageable) {
        UUID userId = (securityUser.getUserEntity().getIdUser());
        return ResponseEntity.ok(orderService.listarPorUsuario(userId, pageable));
    }

    // ── Comprobante por trackingId (acceso público con token) ────────────────

    @GetMapping("/orders/tracking/{trackingId}")
    public ResponseEntity<OrderResponseDTO> getByTracking(
            @PathVariable UUID trackingId) {
        return ResponseEntity.ok(orderService.getByTrackingId(trackingId));
    }
}