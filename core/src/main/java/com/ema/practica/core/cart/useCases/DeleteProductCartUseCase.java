package com.ema.practica.core.cart.useCases;

import com.ema.practica.core.cart.input.IDeleteProductCartInput;
import com.ema.practica.core.cart.model.Cart;
import com.ema.practica.core.cart.output.IRepositoryDeleteProductCart;

public class DeleteProductCartUseCase implements IDeleteProductCartInput {

    IRepositoryDeleteProductCart iRepositoryDeleteProductCart;
    public  DeleteProductCartUseCase(IRepositoryDeleteProductCart iRepositoryDeleteProductCart){
        this.iRepositoryDeleteProductCart = iRepositoryDeleteProductCart;
    }
    @Override
    public boolean deleteCartProduct(Cart CartProduct) {
        if(iRepositoryDeleteProductCart.existeCart(CartProduct)){
            return iRepositoryDeleteProductCart.deleteCart(CartProduct.getIdCart());
        }
        return false;
    }
}
