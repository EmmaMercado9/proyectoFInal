package com.ema.practica.infrastructure.importacion.rest.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Un modelo de la factura (código de artículo) = un producto con sus talles. */
@Data
public class ModeloImportadoDTO {
    @NotBlank
    private String codigoArticulo;   // SKU del producto
    @NotBlank
    private String nombre;
    private String marca;
    private Long subCategoriaId;     // solo para productos nuevos

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal precioVenta;  // para las variantes nuevas

    // Solo vista previa: si el producto ya existe en el sistema
    private UUID productoId;
    private String productoExistente;

    @NotEmpty
    @Valid
    private List<TalleImportadoDTO> talles = new ArrayList<>();
}
