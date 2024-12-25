package com.ema.practica.infrastructure.user.crud;

import com.ema.practica.core.user.model.User;
import com.ema.practica.infrastructure.product.entity.ProductEntity;
import com.ema.practica.infrastructure.user.entitty.UserEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IDeleteUserCrud extends CrudRepository<UserEntity,Long> {

    boolean existsByName(String name);
}
