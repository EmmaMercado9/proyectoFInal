package com.ema.practica.core.user.usecases;

import com.ema.practica.core.user.input.IDeleteUserInput;
import com.ema.practica.core.user.model.User;
import com.ema.practica.core.user.output.IRepositoryDeleteUser;

public class DeleteUserUseCase implements IDeleteUserInput {

    IRepositoryDeleteUser iRepositoryDeleteUser;
    public  DeleteUserUseCase(IRepositoryDeleteUser iRepositoryDeleteUser){
        this.iRepositoryDeleteUser=iRepositoryDeleteUser;
    }

    @Override
    public boolean DeleteUserInput(User user) {
        if(iRepositoryDeleteUser.userExists(user.getName())){
            return iRepositoryDeleteUser.deleteUser(user.getIdUser());
        }
        return false;
    }
}
