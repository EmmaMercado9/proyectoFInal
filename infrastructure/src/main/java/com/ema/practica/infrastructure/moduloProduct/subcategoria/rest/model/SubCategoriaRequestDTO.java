package com.ema.practica.infrastructure.moduloProduct.subcategoria.rest.model;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// SubCategoriaRequestDTO.java
@Data
public class SubCategoriaRequestDTO {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotNull(message = "Debe indicar la categoría padre")
    private Long categoryId;
}