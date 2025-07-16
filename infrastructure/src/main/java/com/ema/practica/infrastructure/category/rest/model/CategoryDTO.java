package com.ema.practica.infrastructure.category.rest.model;

import com.ema.practica.infrastructure.category.entity.CategoryProductEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@Data

public class CategoryDTO {


    @JsonProperty(value="idCategory")
    private  Long idCategory;
    @JsonProperty(value="nameCategory")
    private String nameCategory;



    public CategoryProductEntity dtoToEntity() {
        return new CategoryProductEntity(idCategory,nameCategory);
    }

    // Método para convertir de Core a DTO
    public  static CategoryDTO entityToDTO(CategoryProductEntity categoryProductEntity) {
        return new CategoryDTO(
                categoryProductEntity.getIdCategory(),
                categoryProductEntity.getNameCategory()
        );
    }


}
