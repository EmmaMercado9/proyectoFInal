package com.ema.practica.infrastructure.user.useCases;

import com.ema.practica.core.user.input.IDeleteUserInput;
import com.ema.practica.core.user.model.User;
import com.ema.practica.core.user.output.IRepositoryDeleteUser;
import com.ema.practica.infrastructure.user.crud.IDeleteUserCrud;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeleteUserUseCaseImp implements IRepositoryDeleteUser {

    private final IDeleteUserCrud iDeleteUserCrud;
    @Autowired
    public DeleteUserUseCaseImp(IDeleteUserCrud iDeleteUserCrud){
        this.iDeleteUserCrud=iDeleteUserCrud;
    }
    @Override
    public boolean userExists(String userName) {
        return iDeleteUserCrud.existsByName(userName);
    }

    @Override
    public boolean deleteUser(Long userId) {
        iDeleteUserCrud.deleteById(userId);
        return true;
    }
}
