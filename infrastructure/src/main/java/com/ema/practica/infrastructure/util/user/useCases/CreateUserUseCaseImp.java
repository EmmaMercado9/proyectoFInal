package com.ema.practica.infrastructure.user.useCases;

import com.ema.practica.infrastructure.user.crud.ICreateUserCrud;
import com.ema.practica.infrastructure.user.entitty.UserEntity;
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

    public boolean userNameExist(String userName) {
        return iCreateUserCrud.existsByName(userName);
    }


    public boolean SaveUser(UserEntity userEntity) {

        return (iCreateUserCrud.save(userEntity)).getIdUser()!=null;
    }
}
