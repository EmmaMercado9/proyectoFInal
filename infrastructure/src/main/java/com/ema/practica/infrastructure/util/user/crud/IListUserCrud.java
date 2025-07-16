package com.ema.practica.infrastructure.user.crud;

import com.ema.practica.core.user.model.User;
import com.ema.practica.infrastructure.user.entitty.UserEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
@Repository
public interface IListUserCrud extends CrudRepository<UserEntity,Long> {

    UserEntity findByIdUser(Long id);
    UserEntity findByMail(String email);
    UserEntity findByName(String name);

}
