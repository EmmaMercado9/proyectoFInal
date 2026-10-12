package com.ema.practica.infrastructure.util.user.crud;

import com.ema.practica.infrastructure.util.user.entity.UserEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IDeleteUserCrud extends CrudRepository<UserEntity,UUID> {

    boolean existsByName(String name);
    boolean existsByIdUser(UUID uuid);

    void deleteByIdUser(UUID uuid);
}
