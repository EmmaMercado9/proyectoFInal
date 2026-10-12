package com.ema.practica.infrastructure.moduloProduct.subcategoria.rest.model;

import lombok.Data;

// SubCategoriaResponseDTO.java
@Data
public class SubCategoriaResponseDTO {
    private Long subId;
    private String nombre;
    private String status;
    private Long categoryId;
    private String categoryNombre;
    private Integer cantidadProductos;
}