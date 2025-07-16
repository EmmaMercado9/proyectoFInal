package com.ema.practica.infrastructure.util.user.useCases;

import com.ema.practica.infrastructure.util.status.StatusEntity;
import com.ema.practica.infrastructure.util.user.crud.ILoginUserCrud;
import com.ema.practica.infrastructure.util.user.entitty.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service

public class LoginUserUseCaseImp {

    private final ILoginUserCrud iLoginUserCrud;
    @Autowired
    public LoginUserUseCaseImp(ILoginUserCrud iLoginUserCrud){
        this.iLoginUserCrud = iLoginUserCrud;
    }



    public boolean loginValidateStatus(UserEntity userEntity){
        return userEntity.getStatus() == StatusEntity.ACTIVO;

    }
}
