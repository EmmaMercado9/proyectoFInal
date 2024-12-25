package com.ema.practica.core.user.output;

import com.ema.practica.core.user.model.User;

public interface IRepositoryLogin {
    User searchUserByName(String userName);
}
