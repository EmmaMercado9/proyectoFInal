package com.ema.practica.infrastructure.images.rest;

import com.ema.practica.infrastructure.images.services.ImagenRemotaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Solo ADMIN: entra en la regla de escritura de /api/productos/** (ver SecurityConfig). */
@RestController
@RequiredArgsConstructor
public class ImagenRemotaController {

    private final ImagenRemotaService service;

    @PostMapping("/api/productos/imagen-desde-url")
    public ImagenRemotaService.ImagenRemota descargar(@RequestBody Map<String, String> body) {
        return service.descargar(body.get("url"));
    }
}
