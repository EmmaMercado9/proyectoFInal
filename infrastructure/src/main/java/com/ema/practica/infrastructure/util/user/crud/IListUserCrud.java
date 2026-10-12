package com.ema.practica.infrastructure.util.user.crud;

import com.ema.practica.infrastructure.util.user.entity.UserEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IListUserCrud extends CrudRepository<UserEntity, UUID> {

    UserEntity findByMail(String email);


}
