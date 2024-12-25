package com.ema.practica.core.cart.output;

import com.ema.practica.core.cart.model.Cart;

import java.util.Collection;

public interface IRepositoryConsultCart {

    Collection<Cart> getCartList();
}
