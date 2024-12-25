package com.ema.practica.infrastructure.user.useCases;

import com.ema.practica.core.user.model.User;
import com.ema.practica.core.user.output.IRepositoryModifyUser;
import com.ema.practica.infrastructure.home.crud.IConsultHomeCrud;
import com.ema.practica.infrastructure.home.entity.HomeEntity;
import com.ema.practica.infrastructure.user.crud.IModifyUserCrud;
import com.ema.practica.infrastructure.user.entitty.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ModifyUserUseCaseImp implements IRepositoryModifyUser {
    private final IModifyUserCrud iModifyUserCrud;
    private IConsultHomeCrud iConsultHomeCrud;
    @Autowired
    public  ModifyUserUseCaseImp(IModifyUserCrud iModifyUserCrud){
        this.iModifyUserCrud=iModifyUserCrud;
    }
    @Override
    public boolean userMailExist(String mail) {
        return iModifyUserCrud.searchUserEntitiesByMail(mail);
    }

    @Override
    public boolean userNameExist(String userName) {
        return iModifyUserCrud.searchUserEntitiesByName(userName);
    }

    @Override
    public User get_Current_User(Long idCurrentUser) {
        UserEntity userEntity=iModifyUserCrud.findAllByIdUser(idCurrentUser);
        return (userEntity.entityToCore());
    }

    @Override
    public boolean modifyUser(User user, Long idCurrentUser) throws Exception {
        UserEntity userEntity=iModifyUserCrud.findAllByIdUser(idCurrentUser);
        if(userEntity!=null) {
            userEntity.setName(user.getName());
            userEntity.setMail(user.getMail());
            userEntity.setPassword(user.getPassword());

            if (user.getHome() != null && user.getHome().getIdHome() != null) {
                HomeEntity homeEntity =iConsultHomeCrud.findByIdHome(user.getHome().getIdHome());
                if (homeEntity != null) {
                    userEntity.setHome(homeEntity); // Asigna el home existente
                } else {
                    // Manejar el caso en que el Home no existe
                    throw new Exception("El home no existe");
                }
            }

            iModifyUserCrud.save(userEntity);
            return true;
        }
        return false;
    }
}

