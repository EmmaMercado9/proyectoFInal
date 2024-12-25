package com.ema.practica.infrastructure.cart.rest.model;

import com.ema.practica.core.cart.model.Cart;
import com.ema.practica.core.category.model.CategoryProduct;
import com.ema.practica.core.detailOrder.model.DetailOrder;
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

public class ProductCartDTO {


    @JsonProperty(value="idCart")
    private Long idCart;
    @JsonProperty(value="user")

    private User user;
    @JsonProperty(value="product")

    private Product product;
    @JsonProperty(value="cantidad")

    private int cantidad;

    public    Cart dtoToCore() {
        return Cart.createCart(idCart,user,product,cantidad);
    }

    // Método para convertir de Core a DTO
    public  static ProductCartDTO coreToDTO(Cart cart) {
        return new ProductCartDTO(
               cart.getIdCart(),
                cart.getUser(),
                cart.getProduct(),
                cart.getCantidad()
        );
    }


}
