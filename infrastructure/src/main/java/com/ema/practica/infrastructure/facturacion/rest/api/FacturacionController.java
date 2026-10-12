package com.ema.practica.infrastructure.facturacion.rest.api;

import com.ema.practica.infrastructure.facturacion.rest.model.ConfiguracionFacturacionDTO;
import com.ema.practica.infrastructure.facturacion.rest.model.EstadoFacturacionDTO;
import com.ema.practica.infrastructure.facturacion.useCases.ConfiguracionArcaService;
import com.ema.practica.infrastructure.facturacion.rest.model.FacturaResponseDTO;
import com.ema.practica.infrastructure.facturacion.rest.model.FacturarRequestDTO;
import com.ema.practica.infrastructure.facturacion.rest.model.NotaCreditoRequestDTO;
import com.ema.practica.infrastructure.facturacion.useCases.FacturacionService;
import com.ema.practica.infrastructure.util.user.entity.SecurityUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// Facturación electrónica ARCA. Permisos en SecurityConfig.
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class FacturacionController {

    private final FacturacionService facturacionService;
    private final ConfiguracionArcaService configuracion;

    // ── Configuración (solo ADMIN, ver SecurityConfig) ──────────────────────

    @GetMapping("/facturacion/config")
    public ResponseEntity<ConfiguracionFacturacionDTO> config() {
        return ResponseEntity.ok(configuracion.dto());
    }

    @PutMapping("/facturacion/config")
    public ResponseEntity<ConfiguracionFacturacionDTO> guardarConfig(
            @RequestBody ConfiguracionFacturacionDTO dto,
            @AuthenticationPrincipal SecurityUser user) {
        return ResponseEntity.ok(configuracion.guardar(dto, user != null ? user.getUserEntity().getName() : null));
    }

    // Genera clave (queda en el servidor) + CSR para subir a ARCA
    @PostMapping(value = "/facturacion/csr", produces = "text/plain")
    public ResponseEntity<String> generarCsr(@RequestParam(defaultValue = "false") boolean reemplazar) {
        return ResponseEntity.ok(configuracion.generarCsr(reemplazar));
    }

    @GetMapping(value = "/facturacion/csr", produces = "text/plain")
    public ResponseEntity<String> csr() {
        return ResponseEntity.ok(configuracion.csrActual());
    }

    // Body: { "pem": "-----BEGIN CERTIFICATE-----..." }
    @PostMapping("/facturacion/certificado")
    public ResponseEntity<ConfiguracionFacturacionDTO> certificado(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(configuracion.instalarCertificado(body.getOrDefault("pem", "")));
    }

    // Si está configurada (para mostrar u ocultar el botón "Facturar")
    @GetMapping("/facturacion/estado")
    public ResponseEntity<EstadoFacturacionDTO> estado() {
        return ResponseEntity.ok(facturacionService.estado());
    }

    // Prueba servidores de ARCA y el certificado (solo ADMIN)
    @PostMapping("/facturacion/probar")
    public ResponseEntity<EstadoFacturacionDTO> probar() {
        return ResponseEntity.ok(facturacionService.probarConexion());
    }

    @PostMapping("/orders/{idOrder}/factura")
    public ResponseEntity<FacturaResponseDTO> facturar(
            @PathVariable Long idOrder,
            @RequestBody(required = false) FacturarRequestDTO req,
            @AuthenticationPrincipal SecurityUser user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(facturacionService.facturar(
                idOrder, req, user != null ? user.getUserEntity() : null));
    }

    @GetMapping("/orders/{idOrder}/facturas")
    public ResponseEntity<List<FacturaResponseDTO>> porOrden(@PathVariable Long idOrder) {
        return ResponseEntity.ok(facturacionService.porOrden(idOrder));
    }

    @GetMapping("/facturas/{idFactura}")
    public ResponseEntity<FacturaResponseDTO> obtener(@PathVariable Long idFactura) {
        return ResponseEntity.ok(facturacionService.obtener(idFactura));
    }

    @PostMapping("/facturas/{idFactura}/nota-credito")
    public ResponseEntity<FacturaResponseDTO> notaCredito(
            @PathVariable Long idFactura,
            @RequestBody(required = false) @Valid NotaCreditoRequestDTO req,
            @AuthenticationPrincipal SecurityUser user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(facturacionService.notaCredito(
                idFactura, req != null ? req : new NotaCreditoRequestDTO(),
                user != null ? user.getUserEntity() : null));
    }
}
