package com.ema.practica.core.user.usecases;

import com.ema.practica.core.user.exceptions.ExceptionUserExist;
import com.ema.practica.core.user.exceptions.ExceptionUserNameExist;
import com.ema.practica.core.user.input.ICreateUserInput;
import com.ema.practica.core.user.model.User;
import com.ema.practica.core.user.output.IRepositoryCreateUser;

public class CreateUserUseCase implements ICreateUserInput {

    IRepositoryCreateUser iRepositoryCreateUser;
    public CreateUserUseCase(IRepositoryCreateUser iRepositoryCreateUser) {
    this.iRepositoryCreateUser=iRepositoryCreateUser;

    }

    @Override
    public boolean createUser(User user) {
        if(iRepositoryCreateUser.UserExist(user.getMail())){
            throw new ExceptionUserExist("El usuario ya existe");
        }
        if(iRepositoryCreateUser.userNameExist(user.getName())){
            throw new ExceptionUserNameExist("El nombre de Usuario ingresado ya existe, Ingrese otro!!!");
        }
        return iRepositoryCreateUser.SaveUser(user);
    }
}
