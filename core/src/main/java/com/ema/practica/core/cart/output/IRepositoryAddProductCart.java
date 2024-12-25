package com.ema.practica.core.cart.output;


import com.ema.practica.core.cart.model.Cart;

public interface IRepositoryAddProductCart {

    Cart save(String name,Long id, int cantidad) throws Exception;

}
