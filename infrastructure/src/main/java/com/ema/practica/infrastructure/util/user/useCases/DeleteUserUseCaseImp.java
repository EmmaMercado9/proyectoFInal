package com.ema.practica.infrastructure.util.user.useCases;

import com.ema.practica.infrastructure.util.user.crud.IDeleteUserCrud;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

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


    public boolean deleteUser(UUID userId) {
        iDeleteUserCrud.deleteByIdUser(userId);
        return true;
    }
}
