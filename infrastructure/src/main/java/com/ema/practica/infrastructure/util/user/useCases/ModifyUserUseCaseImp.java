package com.ema.practica.infrastructure.util.user.useCases;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.ema.practica.infrastructure.util.user.crud.IModifyUserCrud;
import com.ema.practica.infrastructure.util.user.entity.UserEntity;
import com.ema.practica.infrastructure.util.user.rest.model.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class ModifyUserUseCaseImp {
    private final IModifyUserCrud iModifyUserCrud;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    public  ModifyUserUseCaseImp(IModifyUserCrud iModifyUserCrud, PasswordEncoder passwordEncoder){
        this.iModifyUserCrud=iModifyUserCrud;
        this.passwordEncoder=passwordEncoder;
    }

    public boolean userMailExist(String mail) {
        return iModifyUserCrud.searchUserEntitiesByMail(mail);
    }

    public boolean userNameExist(String userName) {
        return iModifyUserCrud.searchUserEntitiesByName(userName);
    }

    public UserEntity get_Current_User(UUID idCurrentUser) {
        return (iModifyUserCrud.findAllByIdUser(idCurrentUser));
    }


    public boolean modifyUser(UserDTO user, UUID idCurrentUser) throws Exception {
        UserEntity userEntity=iModifyUserCrud.findAllByIdUser(idCurrentUser);
        if(userEntity!=null) {
            userEntity.setName(user.getName());
            userEntity.setMail(user.getMail());
            // Solo se cambia si viene una nueva; siempre hasheada
            if (user.getPassword() != null && !user.getPassword().isBlank()) {
                userEntity.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            iModifyUserCrud.save(userEntity);
            return true;
        }
        return false;
    }
}

