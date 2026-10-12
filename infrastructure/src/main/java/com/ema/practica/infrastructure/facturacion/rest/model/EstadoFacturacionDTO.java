package com.ema.practica.infrastructure.facturacion.rest.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class EstadoFacturacionDTO {
    private boolean habilitada;          // configurada y lista para facturar
    private String ambiente;             // HOMOLOGACION | PRODUCCION
    private Long cuit;
    private Integer puntoVenta;
    private String razonSocial;
    private List<String> faltantes;      // variables de entorno sin configurar
    private Map<String, String> servidores; // solo en /probar
    private String mensaje;
}
