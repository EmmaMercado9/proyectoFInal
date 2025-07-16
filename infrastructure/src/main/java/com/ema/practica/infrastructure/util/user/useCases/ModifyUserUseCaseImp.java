package com.ema.practica.infrastructure.user.useCases;

import com.ema.practica.infrastructure.user.crud.IModifyUserCrud;
import com.ema.practica.infrastructure.user.entitty.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ModifyUserUseCaseImp {
    private final IModifyUserCrud iModifyUserCrud;
    @Autowired
    public  ModifyUserUseCaseImp(IModifyUserCrud iModifyUserCrud){
        this.iModifyUserCrud=iModifyUserCrud;
    }

    public boolean userMailExist(String mail) {
        return iModifyUserCrud.searchUserEntitiesByMail(mail);
    }

    public boolean userNameExist(String userName) {
        return iModifyUserCrud.searchUserEntitiesByName(userName);
    }

    public UserEntity get_Current_User(Long idCurrentUser) {
        return (iModifyUserCrud.findAllByIdUser(idCurrentUser));
    }


    public boolean modifyUser(UserEntity user, Long idCurrentUser) throws Exception {
        UserEntity userEntity=iModifyUserCrud.findAllByIdUser(idCurrentUser);
        if(userEntity!=null) {
            userEntity.setName(user.getName());
            userEntity.setMail(user.getMail());
            userEntity.setPassword(user.getPassword());
            iModifyUserCrud.save(userEntity);
            return true;
        }
        return false;
    }
}

