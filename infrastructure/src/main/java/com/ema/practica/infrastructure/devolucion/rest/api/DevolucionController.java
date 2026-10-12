package com.ema.practica.infrastructure.devolucion.rest.api;

import com.ema.practica.infrastructure.devolucion.rest.model.DevolucionRequestDTO;
import com.ema.practica.infrastructure.devolucion.rest.model.DevolucionResponseDTO;
import com.ema.practica.infrastructure.devolucion.useCases.DevolucionService;
import com.ema.practica.infrastructure.util.user.entity.SecurityUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// ADMIN y COLABORADOR (ver SecurityConfig)
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class DevolucionController {

    private final DevolucionService devolucionService;

    @PostMapping("/orders/{idOrder}/devoluciones")
    public ResponseEntity<DevolucionResponseDTO> registrar(
            @PathVariable Long idOrder,
            @RequestBody @Valid DevolucionRequestDTO dto,
            @AuthenticationPrincipal SecurityUser securityUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(devolucionService.registrar(
                idOrder, dto, securityUser != null ? securityUser.getUserEntity() : null));
    }

    @GetMapping("/orders/{idOrder}/devoluciones")
    public ResponseEntity<List<DevolucionResponseDTO>> listarPorOrden(@PathVariable Long idOrder) {
        return ResponseEntity.ok(devolucionService.listarPorOrden(idOrder));
    }

    @GetMapping("/devoluciones")
    public ResponseEntity<Page<DevolucionResponseDTO>> listar(Pageable pageable) {
        return ResponseEntity.ok(devolucionService.listar(pageable));
    }
}
