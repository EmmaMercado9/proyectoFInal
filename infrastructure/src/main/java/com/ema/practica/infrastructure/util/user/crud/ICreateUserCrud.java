package com.ema.practica.infrastructure.util.user.crud;

import com.ema.practica.infrastructure.util.user.entity.UserEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ICreateUserCrud  extends CrudRepository<UserEntity, UUID> {

    boolean existsByMail(String email);
    Optional<UserEntity> findByName(String username);


}
