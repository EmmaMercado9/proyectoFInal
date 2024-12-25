package com.ema.practica.infrastructure.cart.crud;

import com.ema.practica.core.cart.model.Cart;
import com.ema.practica.infrastructure.cart.entity.CartEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IDeleteProductCartCrud extends CrudRepository<CartEntity,Long> {


}
