package com.ema.practica.infrastructure.category.rest.model;

import com.ema.practica.core.cart.model.Cart;
import com.ema.practica.core.category.model.CategoryProduct;
import com.ema.practica.core.product.model.Product;
import com.ema.practica.core.user.model.User;
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



    public    CategoryProduct dtoToCore() {
        return CategoryProduct.Instance(idCategory,nameCategory);
    }

    // Método para convertir de Core a DTO
    public  static CategoryDTO coreToDTO(CategoryProduct categoryProduct) {
        return new CategoryDTO(
                categoryProduct.getIdCategory(),
                categoryProduct.getNameCategory()
        );
    }


}
