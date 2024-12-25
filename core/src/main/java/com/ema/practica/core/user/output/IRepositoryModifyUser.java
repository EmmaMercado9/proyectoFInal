package com.ema.practica.core.user.output;

import com.ema.practica.core.user.model.User;

public interface IRepositoryModifyUser {
    boolean userMailExist(String mail);
    boolean userNameExist(String userName);
    User get_Current_User(Long idCurrentUser);
    boolean modifyUser(User user,Long idCurrentUser) throws Exception;
}
