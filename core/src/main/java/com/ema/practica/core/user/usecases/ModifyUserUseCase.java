package com.ema.practica.core.user.usecases;

import com.ema.practica.core.home.model.Home;
import com.ema.practica.core.user.exceptions.ExceptionModifyUserIncorrect;
import com.ema.practica.core.user.input.IModifyUserInput;
import com.ema.practica.core.user.model.User;
import com.ema.practica.core.user.output.IRepositoryModifyUser;

import java.util.Objects;

public class ModifyUserUseCase implements IModifyUserInput {

    IRepositoryModifyUser iRepositoryModifyUser;
    public ModifyUserUseCase(IRepositoryModifyUser iRepositoryModifyUser){
        this.iRepositoryModifyUser=iRepositoryModifyUser;
    }
    @Override
    public boolean ModifyUser(String name, String pass, String mail, Home home, Long idCurrentUser) throws Exception {
        User currentUser = iRepositoryModifyUser.get_Current_User(idCurrentUser);


        if (iRepositoryModifyUser.userMailExist(mail) && !mail.equals(currentUser.getMail())) {
            throw new ExceptionModifyUserIncorrect("El mail ya existe ingrese otro");
        }
        if (iRepositoryModifyUser.userNameExist(name)&& !name.equals(currentUser.getName())) {
            throw new ExceptionModifyUserIncorrect("El nombre de Usuario ingresado ya existe, Ingrese otro!!!");
        }
        currentUser.edit(name,pass,mail,home);
        return iRepositoryModifyUser.modifyUser(currentUser,idCurrentUser);
    }
}
