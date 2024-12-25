package com.ema.practica.core.user.exceptions;
public class ExceptionUserNameExist extends RuntimeException{
    public ExceptionUserNameExist(String msg){
        super(msg);
    }
}