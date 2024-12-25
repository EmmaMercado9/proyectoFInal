package com.ema.practica.infrastructure.user.crud;

import com.ema.practica.infrastructure.user.entitty.UserEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IModifyUserCrud extends CrudRepository<UserEntity,Long> {
    @Query("SELECT COUNT(u) > 0 FROM UserEntity u WHERE u.mail = :mail")
    boolean searchUserEntitiesByMail(@Param("mail") String mail);

    @Query("SELECT COUNT(u) > 0 FROM UserEntity u WHERE u.name = :name")
    boolean searchUserEntitiesByName(@Param("name") String name);

    UserEntity findAllByIdUser(Long id);
}
