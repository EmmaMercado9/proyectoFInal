package com.ema.practica.core.user.input;

import com.ema.practica.core.home.model.Home;

public interface IModifyUserInput {
    boolean ModifyUser(String name, String pass, String mail, Home home, Long idCurrentUser) throws Exception;
}
