package com.ema.practica.infrastructure.importacion.rest.api;

import com.ema.practica.infrastructure.exception.VentaInvalidaException;
import com.ema.practica.infrastructure.importacion.rest.model.FacturaImportadaDTO;
import com.ema.practica.infrastructure.importacion.rest.model.ImportacionFacturaDTO;
import com.ema.practica.infrastructure.importacion.rest.model.ResultadoImportacionDTO;
import com.ema.practica.infrastructure.importacion.useCases.ImportacionFacturaService;
import com.ema.practica.infrastructure.util.user.entity.SecurityUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Importación de productos desde facturas PDF de proveedores.
 * Al estar bajo /api/productos/**, los POST son solo para ADMIN (SecurityConfig).
 */
@RestController
@RequestMapping("/api/productos/importar-factura")
@RequiredArgsConstructor
public class ImportacionFacturaController {

    private final ImportacionFacturaService service;

    /** Historial de facturas importadas, de la más reciente a la más vieja. */
    @GetMapping
    public List<FacturaImportadaDTO> listar() {
        return service.listar();
    }

    /** Paso 1: sube el PDF y devuelve la vista previa (no guarda nada). */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportacionFacturaDTO leer(@RequestParam("archivo") MultipartFile archivo) throws IOException {
        if (archivo.isEmpty()) {
            throw new VentaInvalidaException("El archivo está vacío.");
        }
        return service.leer(archivo.getBytes());
    }

    /** Paso 2: confirma la importación revisada por el usuario. */
    @PostMapping("/confirmar")
    public ResultadoImportacionDTO confirmar(@Valid @RequestBody ImportacionFacturaDTO dto,
                                             @AuthenticationPrincipal SecurityUser usuario) {
        return service.confirmar(dto, usuario != null ? usuario.getUserEntity().getName() : null);
    }
}
