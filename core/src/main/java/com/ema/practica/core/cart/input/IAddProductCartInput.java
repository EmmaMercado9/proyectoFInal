package com.ema.practica.core.cart.input;

import com.ema.practica.core.cart.model.Cart;
import com.ema.practica.core.product.model.Product;

public interface IAddProductCartInput {

    // Aquí deben ir los parámetros necesarios para agregar un producto al carrito
    Cart addProductCart(String name,Long idProduct,int cantidad) throws Exception;


}
