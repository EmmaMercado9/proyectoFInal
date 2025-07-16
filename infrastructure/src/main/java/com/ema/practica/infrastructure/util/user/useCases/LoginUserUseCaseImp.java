package com.ema.practica.infrastructure.user.useCases;

import com.ema.practica.infrastructure.user.crud.ILoginUserCrud;
import com.ema.practica.infrastructure.user.entitty.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service

public class LoginUserUseCaseImp {

    private final ILoginUserCrud iLoginUserCrud;
    @Autowired
    public LoginUserUseCaseImp(ILoginUserCrud iLoginUserCrud){
        this.iLoginUserCrud = iLoginUserCrud;
    }


    public UserEntity searchUserByName(String userName) {
        return  iLoginUserCrud.searchByName(userName);
    }
}
