package com.ema.practica.infrastructure.moduloProduct.productVariant.rest.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

// Datos editables de una variante. Precios null = usa los del producto.
// Atributos (talla/color) no se editan: para eso se crea otra variante.
@Data
public class VarianteUpdateDTO {

    @NotBlank(message = "El SKU es obligatorio")
    private String sku;

    private String codigoBarra;

    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    private BigDecimal precioVenta;

    @DecimalMin(value = "0.0", message = "El precio promocional no puede ser negativo")
    private BigDecimal precioPro;

    @DecimalMin(value = "0.0", message = "El costo no puede ser negativo")
    private BigDecimal costo;

    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    private Integer stockMinimo;
}
