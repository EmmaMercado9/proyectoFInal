package com.ema.practica.infrastructure.user.useCases;

import com.ema.practica.infrastructure.user.crud.IListUserCrud;
import com.ema.practica.infrastructure.user.entitty.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
@Service
public class ListUserUseCaseImp  {
   private final IListUserCrud iListUserCrud;
   @Autowired
   public ListUserUseCaseImp(IListUserCrud iListUserCrud) {
       this.iListUserCrud = iListUserCrud;
   }

    public Collection<UserEntity> getAll() {
        Iterable<UserEntity>  userEntities = iListUserCrud.findAll();
        return StreamSupport.stream(userEntities.spliterator(),false).collect(Collectors.toList());    }

    public UserEntity getUserById(String username){
       UserEntity userEntity=this.iListUserCrud.findByMail(username);
       if(userEntity==null) {
        throw new RuntimeException("User no encontrado");
       }
       return userEntity;
    }
}
