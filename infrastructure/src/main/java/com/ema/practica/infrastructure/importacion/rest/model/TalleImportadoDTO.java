package com.ema.practica.infrastructure.importacion.rest.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/** Un talle de un modelo = una variante. */
@Data
public class TalleImportadoDTO {
    @NotBlank
    private String talle;
    @NotBlank
    private String sku;
    @Min(1)
    private int cantidad;
    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal costoUnitario;

    // Solo vista previa: si la variante ya existe
    private Long varianteId;
    private Integer stockActual;
}
