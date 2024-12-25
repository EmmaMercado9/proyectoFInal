package com.ema.practica.infrastructure.cart.useCases;

import com.ema.practica.core.cart.model.Cart;
import com.ema.practica.core.cart.output.IRepositoryConsultCart;
import com.ema.practica.infrastructure.cart.crud.IConsultCartCrud;
import com.ema.practica.infrastructure.cart.entity.CartEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class ConsultCartUseCaseImp implements IRepositoryConsultCart {

    private  final IConsultCartCrud iConsultCartCrud;

    @Autowired
    public ConsultCartUseCaseImp(IConsultCartCrud iConsultCartCrud) {
        this.iConsultCartCrud = iConsultCartCrud;
    }
    @Override
    public Collection<Cart> getCartList() {
        Iterable<CartEntity> cartEntities=iConsultCartCrud.findAll();

        return StreamSupport.stream(cartEntities.spliterator(), false).map(CartEntity::entityToCore).collect(Collectors.toList());
    }
}
