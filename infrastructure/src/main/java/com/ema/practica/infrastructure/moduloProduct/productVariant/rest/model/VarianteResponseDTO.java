package com.ema.practica.infrastructure.moduloProduct.productVariant.rest.model;

import com.ema.practica.infrastructure.images.ImagenModelDto;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// VarianteResponseDTO.java
@Data
public class VarianteResponseDTO {
    private Long variantId;
    private String sku;
    private String codigoBarra;
    private BigDecimal precioVenta;     // ya resuelto: propio o heredado
    private BigDecimal precioPro;
    private BigDecimal costo;           // propio de la variante (null = no cargado)
    private Integer stock;
    private Integer stockMinimo;
    private Boolean esDefault;
    private String status;
    private Map<String, String> atributos;   // {"Talla":"42", "Color":"Rojo"}
    private String descripcion;
    private List<ImagenModelDto> imageModelSetDto = new ArrayList<>();
// "Talla 42 / Color Rojo"
}