package com.ema.practica.infrastructure.cart.crud;

import com.ema.practica.core.cart.model.Cart;
import com.ema.practica.infrastructure.cart.entity.CartEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface IConsultCartCrud  extends CrudRepository<CartEntity,Long> {


}
