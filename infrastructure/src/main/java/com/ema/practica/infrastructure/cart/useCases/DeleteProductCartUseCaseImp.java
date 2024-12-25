package com.ema.practica.infrastructure.cart.useCases;

import com.ema.practica.core.cart.input.IDeleteProductCartInput;
import com.ema.practica.core.cart.model.Cart;
import com.ema.practica.core.cart.output.IRepositoryDeleteProductCart;
import com.ema.practica.infrastructure.cart.crud.IDeleteProductCartCrud;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeleteProductCartUseCaseImp implements IRepositoryDeleteProductCart {

    private final IDeleteProductCartCrud iDeleteProductCartCrud;
    @Autowired
    public  DeleteProductCartUseCaseImp(IDeleteProductCartCrud iDeleteProductCartCrud) {
        this.iDeleteProductCartCrud = iDeleteProductCartCrud;
    }
    @Override
    public boolean existeCart(Cart cart) {
        return iDeleteProductCartCrud.existsById(cart.getIdCart());
    }

    @Override
    public boolean deleteCart(Long idCart) {
        iDeleteProductCartCrud.deleteById(idCart);
        return true;
    }
}
