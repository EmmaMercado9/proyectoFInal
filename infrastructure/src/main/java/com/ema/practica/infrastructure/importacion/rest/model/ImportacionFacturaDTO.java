package com.ema.practica.infrastructure.importacion.rest.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Factura leída, agrupada por modelo. Se usa en los dos sentidos:
 * el backend la devuelve como vista previa y el front la manda de vuelta,
 * con lo que el usuario corrigió, para confirmar la importación.
 */
@Data
public class ImportacionFacturaDTO {
    @NotBlank
    private String proveedor;
    @NotBlank
    private String cuit;
    @NotBlank
    private String numero;
    private LocalDate fecha;
    private BigDecimal total;

    // Solo vista previa
    private BigDecimal sumaLineas;
    private boolean totalCoincide;
    private String yaImportada;      // mensaje si esa factura ya se cargó
    private int unidades;

    @NotEmpty
    @Valid
    private List<ModeloImportadoDTO> modelos = new ArrayList<>();
}
