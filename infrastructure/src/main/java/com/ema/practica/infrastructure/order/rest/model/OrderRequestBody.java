package com.ema.practica.infrastructure.order.rest.model;

import com.ema.practica.core.cart.model.Cart;
import com.ema.practica.core.user.model.User;
import com.ema.practica.infrastructure.cart.entity.CartEntity;
import com.ema.practica.infrastructure.cart.rest.model.ProductCartDTO;
import com.ema.practica.infrastructure.user.entitty.UserEntity;
import com.ema.practica.infrastructure.user.rest.model.UserDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@Data
@NoArgsConstructor

public class OrderRequestBody {
    @JsonProperty(value = "listCart")
    private List<CartEntity> listCart;
}
