package com.ema.practica.core.cart.output;

import com.ema.practica.core.cart.model.Cart;


public interface IRepositoryDeleteProductCart {

    boolean existeCart(Cart cart);
    boolean deleteCart(Long idCart);

}
