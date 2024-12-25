package com.ema.practica.infrastructure.user.useCases;

import com.ema.practica.core.user.model.User;
import com.ema.practica.core.user.output.IRepositoryLogin;
import com.ema.practica.infrastructure.user.crud.ILoginUserCrud;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service

public class LoginUserUseCaseImp implements IRepositoryLogin {

    private final ILoginUserCrud iLoginUserCrud;
    @Autowired
    public LoginUserUseCaseImp(ILoginUserCrud iLoginUserCrud){
        this.iLoginUserCrud = iLoginUserCrud;
    }

    @Override
    public User searchUserByName(String userName) {
        return  iLoginUserCrud.searchByName(userName).entityToCore();
    }
}
