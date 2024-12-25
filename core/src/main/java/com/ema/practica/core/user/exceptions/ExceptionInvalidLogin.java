package com.ema.practica.core.user.exceptions;
public class ExceptionInvalidLogin extends RuntimeException{
    public ExceptionInvalidLogin (String msg){
        super(msg);
    }
}