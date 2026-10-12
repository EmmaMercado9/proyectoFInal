package com.ema.practica.infrastructure.moduloProduct.category.rest.mapper;

import com.ema.practica.infrastructure.moduloProduct.category.entity.CategoryProductEntity;
import com.ema.practica.infrastructure.moduloProduct.category.rest.model.CategoryResponseDTO;
import com.ema.practica.infrastructure.moduloProduct.subcategoria.entity.SubCategoriaEntity;
import com.ema.practica.infrastructure.moduloProduct.subcategoria.rest.model.SubCategoriaResponseDTO;
import org.springframework.stereotype.Component;

// CategoryMapper.java
@Component
public class CategoryMapper {

    public CategoryResponseDTO toResponseDTO(CategoryProductEntity entity) {
        CategoryResponseDTO dto = new CategoryResponseDTO();
        dto.setIdCategory(entity.getIdCategory());
        dto.setNameCategory(entity.getNameCategory());
        dto.setStatus(entity.getStatus().name());
        dto.setCantidadSubcategorias(entity.getSubCategorias().size());
        dto.setCantidadProductos(entity.getCantidadProductos());
        dto.setSubCategorias(
                entity.getSubCategorias().stream()
                        .map(this::toSubDTO)
                        .toList()
        );
        return dto;
    }

    public SubCategoriaResponseDTO toSubDTO(SubCategoriaEntity sub) {
        SubCategoriaResponseDTO dto = new SubCategoriaResponseDTO();
        dto.setSubId(sub.getSubId());
        dto.setNombre(sub.getNombre());
        dto.setStatus(sub.getStatus().name());
        dto.setCategoryId(sub.getCategoryProductEntity().getIdCategory());
        dto.setCategoryNombre(sub.getCategoryProductEntity().getNameCategory());
        dto.setCantidadProductos(sub.getProductEntityList().size());
        return dto;
    }
}