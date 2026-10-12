package com.ema.practica.infrastructure.moduloProduct.category.rest.model;

import com.ema.practica.infrastructure.moduloProduct.subcategoria.rest.model.SubCategoriaResponseDTO;
import lombok.Data;

import java.util.List;

// CategoryResponseDTO.java
@Data
public class CategoryResponseDTO {
    private Long idCategory;
    private String nameCategory;
    private String status;
    private Integer cantidadSubcategorias;
    private Integer cantidadProductos;
    private List<SubCategoriaResponseDTO> subCategorias;
}