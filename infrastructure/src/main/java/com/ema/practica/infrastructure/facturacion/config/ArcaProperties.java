package com.ema.practica.infrastructure.facturacion.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuración de facturación electrónica ARCA (ex AFIP). Todo por variables
 * de entorno: ver application.properties y FACTURACION-ARCA.md.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "arca")
public class ArcaProperties {

    public enum Ambiente { HOMOLOGACION, PRODUCCION }

    private boolean enabled = false;
    private Ambiente ambiente = Ambiente.HOMOLOGACION;

    // Seguro: producción (facturas reales, con impacto fiscal) exige además
    // ARCA_PERMITIR_PRODUCCION=true. Sin eso solo se puede facturar en prueba.
    private boolean permitirProduccion = false;

    public boolean produccionBloqueada() {
        return ambiente == Ambiente.PRODUCCION && !permitirProduccion;
    }

    // Emisor
    private Long cuit;
    private Integer puntoVenta;
    private String razonSocial;
    private String domicilio;
    private String iibb;
    private String inicioActividades;          // dd/MM/yyyy, se imprime en la factura
    private String condicionIva = "Responsable Monotributo";

    // Certificado X.509 que entrega ARCA (.crt) y la clave privada con la que se generó el CSR
    private String certPath;
    private String keyPath;

    // Dónde se guarda el ticket de acceso (dura 12 h) para sobrevivir reinicios
    private String taCacheDir = System.getProperty("user.home") + "/.tienda-arca";

    // URLs: si no se configuran, se usan las del ambiente
    private String wsaaUrl;
    private String wsfeUrl;

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

    /** Qué falta configurar para poder facturar (vacío = listo) */
    public List<String> faltantes() {
        List<String> f = new ArrayList<>();
        if (!enabled) f.add("ARCA_ENABLED=true");
        if (cuit == null) f.add("ARCA_CUIT");
        if (puntoVenta == null) f.add("ARCA_PUNTO_VENTA");
        if (blank(certPath)) f.add("ARCA_CERT_PATH");
        if (blank(keyPath)) f.add("ARCA_KEY_PATH");
        if (blank(razonSocial)) f.add("ARCA_RAZON_SOCIAL");
        if (produccionBloqueada()) f.add("ARCA_PERMITIR_PRODUCCION=true (producción bloqueada: se factura solo en prueba)");
        return f;
    }

    static final String MSG_BLOQUEO = "Facturación real (PRODUCCION) bloqueada: "
            + "configurá ARCA_PERMITIR_PRODUCCION=true solo cuando quieras emitir comprobantes con validez fiscal";

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }
}
