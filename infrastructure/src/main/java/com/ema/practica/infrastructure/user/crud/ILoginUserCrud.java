package com.ema.practica.infrastructure.user.crud;

import com.ema.practica.infrastructure.user.entitty.UserEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ILoginUserCrud extends CrudRepository<UserEntity,Long> {

    UserEntity searchByName(String name);

    UserEntity findFirstByMail(String mail);
}
