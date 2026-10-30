package com.ema.practica.infrastructure.pagos.rest.api;

import com.ema.practica.infrastructure.pagos.rest.model.PlanPagoDTO;
import com.ema.practica.infrastructure.pagos.useCases.PlanPagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Planes de pago. Lectura para ADMIN y COLABORADOR (el punto de venta los necesita);
 * modificación solo ADMIN (ver SecurityConfig).
 */
@RestController
@RequiredArgsConstructor
public class PlanPagoController {

    private final PlanPagoService service;

    @GetMapping("/api/pagos/planes")
    public List<PlanPagoDTO> listar() {
        return service.listar();
    }

    @PutMapping("/api/admin/pagos/planes")
    public List<PlanPagoDTO> guardar(@RequestBody List<@Valid PlanPagoDTO> planes) {
        return service.guardar(planes);
    }
}
