package com.ema.practica.core.user.output;

public interface IRepositoryDeleteUser {

    boolean userExists(String userName);
    boolean deleteUser(Long userId);
}
