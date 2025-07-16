package com.ema.practica.infrastructure.user.useCases;

import com.ema.practica.infrastructure.user.crud.IDeleteUserCrud;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeleteUserUseCaseImp  {

    private final IDeleteUserCrud iDeleteUserCrud;
    @Autowired
    public DeleteUserUseCaseImp(IDeleteUserCrud iDeleteUserCrud){
        this.iDeleteUserCrud=iDeleteUserCrud;
    }

    public boolean userExists(String userName) {
        return iDeleteUserCrud.existsByName(userName);
    }


    public boolean deleteUser(Long userId) {
        iDeleteUserCrud.deleteById(userId);
        return true;
    }
}
