package com.ema.practica.infrastructure.reportes.rest.api;

import com.ema.practica.infrastructure.reportes.rest.model.ReporteDTO;
import com.ema.practica.infrastructure.reportes.useCases.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** Estadísticas y reportes de gestión (solo ADMIN, ver SecurityConfig). */
@RestController
@RequestMapping("/api/admin/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService service;

    /** Por defecto, los últimos 30 días. Fechas en formato yyyy-MM-dd. */
    @GetMapping
    public ReporteDTO reporte(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        LocalDate fin = hasta != null ? hasta : LocalDate.now();
        LocalDate inicio = desde != null ? desde : fin.minusDays(29);
        return service.generar(inicio, fin);
    }
}
