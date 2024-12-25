package com.ema.practica.infrastructure.cart.crud;

import com.ema.practica.infrastructure.cart.entity.CartEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IAddProductCartCrud extends CrudRepository<CartEntity,Long> {
    List<CartEntity> findByUser_IdUser(Long idUsuario);
    List<CartEntity> findByUser_Mail(String name);

    void deleteByUser_IdUser(Long idUsuario);
    Long countByUser_IdUser(Long idUsuario);
}
