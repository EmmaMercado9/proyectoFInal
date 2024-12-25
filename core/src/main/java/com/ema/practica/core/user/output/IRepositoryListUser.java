package com.ema.practica.core.user.output;

import com.ema.practica.core.user.model.User;

import java.util.Collection;

public interface IRepositoryListUser {
    Collection<User> getAll();

}
