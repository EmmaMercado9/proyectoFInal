package com.ema.practica.infrastructure.moduloProduct.category.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;


// CategoryRequestDTO.java
@Data
public class CategoryRequestDTO {
    @NotBlank(message = "El nombre de la categoría es obligatorio")
    private String nameCategory;
}
