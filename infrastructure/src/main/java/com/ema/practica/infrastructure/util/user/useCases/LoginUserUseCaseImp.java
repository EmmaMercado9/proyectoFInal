package com.ema.practica.infrastructure.util.user.useCases;

import com.ema.practica.infrastructure.util.status.StatusEntity;
import com.ema.practica.infrastructure.util.user.crud.ILoginUserCrud;
import com.ema.practica.infrastructure.util.user.entity.UserEntity;
import com.ema.practica.infrastructure.util.user.rest.model.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service

public class LoginUserUseCaseImp {

    private final ILoginUserCrud iLoginUserCrud;
    @Autowired
    public LoginUserUseCaseImp(ILoginUserCrud iLoginUserCrud){
        this.iLoginUserCrud = iLoginUserCrud;
    }



    public boolean loginValidateStatus(StatusEntity statusEntity){
        return statusEntity==StatusEntity.ACTIVO;

    }
}
