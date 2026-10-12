package com.ema.practica.infrastructure.util.user.useCases;

import com.ema.practica.infrastructure.util.user.crud.ICreateUserCrud;
import com.ema.practica.infrastructure.util.user.entity.UserEntity;
import com.ema.practica.infrastructure.util.user.rest.model.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CreateUserUseCaseImp  {

    private final ICreateUserCrud iCreateUserCrud;
    @Autowired
    public CreateUserUseCaseImp(ICreateUserCrud iCreateUserCr){
        this.iCreateUserCrud=iCreateUserCr;
    }


    public boolean UserExist(String mail) {
        return iCreateUserCrud.existsByMail(mail);
    }



    public boolean SaveUser(UserDTO userDTO,String username) {
        UserEntity userEntity=userDTO.dtoToEntity();
        userEntity.setCreatedBy(username);
        return (iCreateUserCrud.save(userEntity)).getIdUser()!=null;
    }
}
