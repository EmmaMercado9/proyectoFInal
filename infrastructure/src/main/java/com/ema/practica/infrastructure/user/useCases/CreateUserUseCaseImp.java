package com.ema.practica.infrastructure.user.useCases;

import com.ema.practica.core.user.model.User;
import com.ema.practica.core.user.output.IRepositoryCreateUser;
import com.ema.practica.infrastructure.home.crud.IConsultHomeCrud;
import com.ema.practica.infrastructure.home.entity.HomeEntity;
import com.ema.practica.infrastructure.user.crud.ICreateUserCrud;
import com.ema.practica.infrastructure.user.entitty.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CreateUserUseCaseImp implements IRepositoryCreateUser {

    private final ICreateUserCrud iCreateUserCrud;
    private final IConsultHomeCrud iConsultHomeCrud;
    @Autowired
    public CreateUserUseCaseImp(ICreateUserCrud iCreateUserCr, IConsultHomeCrud iConsultHomeCrud){
        this.iCreateUserCrud=iCreateUserCr;
        this.iConsultHomeCrud = iConsultHomeCrud;
    }

    @Override
    public boolean UserExist(String mail) {
        return iCreateUserCrud.existsByMail(mail);
    }

    @Override
    public boolean userNameExist(String userName) {
        return iCreateUserCrud.existsByName(userName);
    }

    @Override
    public boolean SaveUser(User user) {
        UserEntity userEntity=UserEntity.coreToEntity(user);
        return (iCreateUserCrud.save(userEntity)).getIdUser()!=null;
    }
}
