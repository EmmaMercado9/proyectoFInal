package com.ema.practica.core.user.output;

import com.ema.practica.core.user.model.User;

public interface IRepositoryCreateUser {
    boolean UserExist(String mail);
    boolean userNameExist(String userName);
    boolean SaveUser(User user);
}
