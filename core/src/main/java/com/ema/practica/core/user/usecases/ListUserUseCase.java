package com.ema.practica.core.user.usecases;

import com.ema.practica.core.user.input.IListUserInput;
import com.ema.practica.core.user.model.User;
import com.ema.practica.core.user.output.IRepositoryListUser;

import java.util.Collection;

public class ListUserUseCase  implements IListUserInput {

    IRepositoryListUser iRepositoryListUser;
    public ListUserUseCase(IRepositoryListUser iRepositoryListUser){
        this.iRepositoryListUser=iRepositoryListUser;
    }
    @Override
    public Collection<User> getAllUsers() {
        return iRepositoryListUser.getAll();
    }
}
