package com.ema.practica.infrastructure.facturacion.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración de infraestructura de ARCA (variables de entorno).
 * Los datos del emisor (CUIT, razón social, punto de venta...) se administran
 * desde el panel: ver ConfiguracionArcaService.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "arca")
public class ArcaProperties {

    public enum Ambiente { HOMOLOGACION, PRODUCCION }

    private Ambiente ambiente = Ambiente.HOMOLOGACION;

    // Seguro: producción (facturas reales, con impacto fiscal) exige además
    // ARCA_PERMITIR_PRODUCCION=true. No se puede cambiar desde el panel.
    private boolean permitirProduccion = false;

    // Carpeta del servidor con la clave privada, el certificado y el ticket de acceso
    private String directorio = System.getProperty("user.home") + "/.tienda-arca";

    // Opcional: rutas propias del certificado/clave (si no, se usan los del directorio)
    private String certPath;
    private String keyPath;

    // URLs: si no se configuran, se usan las del ambiente
    private String wsaaUrl;
    private String wsfeUrl;

    static final String MSG_BLOQUEO = "Facturación real (PRODUCCION) bloqueada: "
            + "configurá ARCA_PERMITIR_PRODUCCION=true solo cuando quieras emitir comprobantes con validez fiscal";

    public boolean produccionBloqueada() {
        return ambiente == Ambiente.PRODUCCION && !permitirProduccion;
    }

    public String wsaaUrlEfectiva() {
        if (produccionBloqueada()) throw new IllegalStateException(MSG_BLOQUEO);
        if (wsaaUrl != null && !wsaaUrl.isBlank()) return wsaaUrl;
        return ambiente == Ambiente.PRODUCCION
                ? "https://wsaa.afip.gov.ar/ws/services/LoginCms"
                : "https://wsaahomo.afip.gov.ar/ws/services/LoginCms";
    }

    public String wsfeUrlEfectiva() {
        if (produccionBloqueada()) throw new IllegalStateException(MSG_BLOQUEO);
        if (wsfeUrl != null && !wsfeUrl.isBlank()) return wsfeUrl;
        return ambiente == Ambiente.PRODUCCION
                ? "https://servicios1.afip.gov.ar/wsfev1/service.asmx"
                : "https://wswhomo.afip.gov.ar/wsfev1/service.asmx";
    }
}
