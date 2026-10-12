package com.ema.practica.infrastructure.facturacion.rest.model;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// Pantalla Configuración → Facturación (solo ADMIN)
@Data
public class ConfiguracionFacturacionDTO {
    // Editables
    private boolean habilitada;
    private Long cuit;
    private Integer puntoVenta;
    private String razonSocial;
    private String domicilio;
    private String iibb;
    private String inicioActividades;     // dd/MM/yyyy

    // Solo lectura
    private String condicionIva;
    private String ambiente;              // HOMOLOGACION | PRODUCCION (lo fija el servidor)
    private boolean produccionBloqueada;
    private boolean archivosDelServidor;  // certificado por variables de entorno
    private boolean claveGenerada;
    private boolean csrDisponible;
    private String certificadoSujeto;
    private Long certificadoCuit;
    private LocalDate certificadoVence;
    private Boolean certificadoCoincide;
    private List<String> faltantes;
    private String actualizadoPor;
    private LocalDateTime actualizadoEn;
}
