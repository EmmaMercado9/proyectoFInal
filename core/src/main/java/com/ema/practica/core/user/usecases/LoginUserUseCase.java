package com.ema.practica.core.user.usecases;

import com.ema.practica.core.user.exceptions.ExceptionInvalidLogin;
import com.ema.practica.core.user.input.ILoginUserInput;
import com.ema.practica.core.user.model.User;
import com.ema.practica.core.user.output.IRepositoryLogin;

public class LoginUserUseCase implements ILoginUserInput {
    IRepositoryLogin iRepositoryLogin;

    public LoginUserUseCase(IRepositoryLogin iRepositoryLogin) {
        this.iRepositoryLogin = iRepositoryLogin;
    }
    @Override
    public boolean LoginUser(String nameUser, String password) {
        User user=iRepositoryLogin.searchUserByName(nameUser);
        if(user!=null && user.getPassword().equals(password)){
            return true;
        }
        else{
            throw new ExceptionInvalidLogin("Ingrese credenciales válidas!!");
        }
    }
}
