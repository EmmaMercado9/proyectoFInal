package com.ema.practica.infrastructure.moduloProduct.product.rest.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

// Datos editables de un producto. No incluye tieneVariantes ni stock:
// el stock se ajusta por variante (PATCH /variantes/{id}/stock)
@Data
public class ProductUpdateDTO {

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

    @DecimalMin(value = "0.0", message = "El precio promocional no puede ser negativo")
    private BigDecimal precioPro;

    @NotNull(message = "El costo es obligatorio")
    @DecimalMin(value = "0.0", message = "El costo no puede ser negativo")
    private BigDecimal costo;

    private Long subCategoriaId;
}
