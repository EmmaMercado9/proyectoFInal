package com.ema.practica.infrastructure.util.user.useCases;

import com.ema.practica.infrastructure.exception.RecursoNoEncontradoException;
import com.ema.practica.infrastructure.util.user.crud.IListUserCrud;
import com.ema.practica.infrastructure.util.user.entity.UserEntity;
import com.ema.practica.infrastructure.util.user.rest.model.UserDtoAdmin;
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

    public Collection<UserDtoAdmin> getAll() {
        Iterable<UserEntity>  userEntities = iListUserCrud.findAll();
        return StreamSupport.stream(userEntities.spliterator(),false).map(UserDtoAdmin::EntityToUserAdmin).collect(Collectors.toList());    }

    public UserEntity getUserById(String username){
       UserEntity userEntity=this.iListUserCrud.findByMail(username);
       if(userEntity==null) {
        throw new RecursoNoEncontradoException("Usuario no encontrado");
       }
       return userEntity;
    }
}
