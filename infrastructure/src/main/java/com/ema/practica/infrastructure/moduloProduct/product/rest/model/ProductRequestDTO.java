package com.ema.practica.infrastructure.moduloProduct.product.rest.model;

import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

// ProductRequestDTO.java
@Data
public class ProductRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    private String nameProduct;

    private String codigoBarra;
    private String sku;
    private String description;
    private String marca;
    private String modelo;

    @NotNull(message = "El precio de venta es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    private BigDecimal precioVenta;

    private BigDecimal precioPro;

    @NotNull(message = "El costo es obligatorio")
    private BigDecimal costo;

    private Long subCategoriaId;

    @NotNull(message = "Debe indicar si el producto tiene variantes")
    private Boolean tieneVariantes;

    // Solo se usa si tieneVariantes = false
    private Integer stockInicial;
    private Integer stockMinimo;
}