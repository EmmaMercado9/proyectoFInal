package com.ema.practica.infrastructure.util.user.crud;

import com.ema.practica.infrastructure.util.user.entitty.UserEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ILoginUserCrud extends CrudRepository<UserEntity, UUID> {


    UserEntity findUserEntityByMail(String mail);
}
