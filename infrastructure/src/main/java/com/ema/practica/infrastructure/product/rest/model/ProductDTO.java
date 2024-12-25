package com.ema.practica.infrastructure.product.rest.model;

import com.ema.practica.core.cart.model.Cart;
import com.ema.practica.core.category.model.CategoryProduct;
import com.ema.practica.core.detailOrder.model.DetailOrder;

import com.ema.practica.core.images.ImageM;
import com.ema.practica.core.product.model.Product;

import com.ema.practica.core.sizes.Size;
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
    private CategoryProduct categoryProduct;
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
    private List<Size> listSize;
    @JsonProperty(value="images")
    private Set<ImageM> imageMSet;
    public    Product dtoToCore() { return Product.getInstance(idProduct,categoryProduct,nameProduct,priceUnitary,discontuedPrice,descriptionProduct,status,color,listSize,imageMSet);
    }

    // Método para convertir de Core a DTO
    public  static ProductDTO coreToDTO(Product product) {
        return new ProductDTO(
                product.getIdProduct(),
                product.getCategory(),
                product.getNameProduct(),
                product.getPriceUnitary(),
                product.getDiscontuedPrice(),
                product.getDescriptionProduct(),
                product.isStatus(),
                product.getColor(),
                product.getSizes(),
                product.getImageMSet()

               );
    }


}
