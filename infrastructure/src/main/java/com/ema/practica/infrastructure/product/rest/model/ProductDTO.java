package com.ema.practica.infrastructure.product.rest.model;


import com.ema.practica.infrastructure.category.entity.CategoryProductEntity;
import com.ema.practica.infrastructure.images.ImageModel;
import com.ema.practica.infrastructure.product.entity.ProductEntity;
import com.ema.practica.infrastructure.sizes.SizeEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductDTO {
    @JsonProperty(value="idProduct")
    private  Long idProduct;
    @JsonProperty(value="category")
    private CategoryProductEntity categoryProductEntity;
    @JsonProperty(value="nameProduct")

    private String nameProduct;
    @JsonProperty(value="priceUnitary")

    private double priceUnitary;
    @JsonProperty(value = "discontuedPrice")
    private double discontuedPrice;
    @JsonProperty(value="descriptionProduct")
    private String descriptionProduct;
    @JsonProperty(value="status")
    private boolean status;
    @JsonProperty(value="color")
    private String color;
    @JsonProperty(value = "listSize")
    private List<SizeEntity> listSize;
    @JsonProperty(value="images")
    private Set<ImageModel> imageMSet;
    public ProductEntity dtoToEntity() { return new ProductEntity(idProduct,categoryProductEntity,nameProduct,priceUnitary,discontuedPrice,descriptionProduct,status,color,listSize,imageMSet);
    }

    // Método para convertir de Core a DTO
    public  static ProductDTO entityToDTO(ProductEntity productEntity) {
        return new ProductDTO(
                productEntity.getIdProduct(),
                productEntity.getCategoryProduct(),
                productEntity.getNameProduct(),
                productEntity.getPriceUnitary(),
                productEntity.getDiscontuedPrice(),
                productEntity.getDescriptionProduct(),
                productEntity.isStatus(),
                productEntity.getColor(),
                productEntity.getSizes(),
                productEntity.getImageModelSet()

               );
    }


}
