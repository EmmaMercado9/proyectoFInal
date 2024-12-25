package com.ema.practica.core.cart.useCases;

import com.ema.practica.core.cart.input.IConsultCartInput;
import com.ema.practica.core.cart.model.Cart;
import com.ema.practica.core.cart.output.IRepositoryConsultCart;

import java.util.Collection;

public class ConsultCartUseCase implements IConsultCartInput {
    IRepositoryConsultCart iRepositoryConsultCart;

    public  ConsultCartUseCase(IRepositoryConsultCart iRepositoryConsultCart){
        this.iRepositoryConsultCart = iRepositoryConsultCart;
    }
    @Override
    public Collection<Cart> getAllCart() {
        return iRepositoryConsultCart.getCartList();
    }
}
