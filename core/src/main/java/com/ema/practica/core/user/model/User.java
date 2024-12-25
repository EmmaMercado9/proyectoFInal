package com.ema.practica.core.user.model;

import com.ema.practica.core.Utils.CheckEmail;
import com.ema.practica.core.Utils.CheckPassword;
import com.ema.practica.core.Utils.CkeckName;
import com.ema.practica.core.home.model.Home;
import com.ema.practica.core.user.exceptions.ExceptionUserIncomplete;
import com.ema.practica.core.user.exceptions.ExceptionUserIncorrect;

public class User {
    private Long idUser;
    private Home home;
    private String role;
    private String name;
    private String mail;
    private String password;
    public User(){}
    private User(Long id,Home home,String name, String mail, String password) {
        this.idUser=id;
        this.home=home;
        this.role=asign(name);
        this.name = name;
        this.mail = mail;
        this.password = password;

    }

    private String asign(String name) {
        if(name.equals("Admin1")){
            return "ADMIN";
        }
        return "USER";
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    private User(Long id, Home home, String name, String password) {
        this.idUser=id;
        this.home=home;
        this.role=asign(name);
        this.name = name;
        this.password = password;

    }
    public static User instance(Long id,Home home,String name, String mail, String password) {
        /*if(!CkeckName.verifyLoadedUser(name)){
            throw new ExceptionUserIncomplete("El nombre usuario debe ser cargado!!");
        }
        if((!CkeckName.verifyUser(name))){
            throw  new ExceptionUserIncorrect("Ingrese un formato adecuado!!");
        }
        if(!CheckEmail.verifyLoadedMail(mail)){
            throw new ExceptionUserIncomplete("El correo electronico debe ser cargado!!");
        }
        else if(!CheckEmail.verifyEmail(mail)){
            throw new ExceptionUserIncorrect("El correo electronico es invalido!!");
        }
        if(!CheckPassword.verifyLoadedPass(password)){
            throw new ExceptionUserIncomplete("La contraseña debe ser cargada!!");
        }
        //else if(!CheckPassword.verifyPassword(password)){
          //  throw new ExceptionUserIncorrect("La contraseña no es valida!!");
        //}
         */
        return new User(id,home,name, mail,password);
    }
    public static User instance(Long id,Home home,String name, String password) {
       /* if(!CkeckName.verifyLoadedUser(name)){
            throw new ExceptionUserIncomplete("El nombre usuario debe ser cargado!!");
        }
        if((!CkeckName.verifyUser(name))){
            throw  new ExceptionUserIncorrect("Ingrese un formato adecuado!!");
        }
        if(!CheckPassword.verifyLoadedPass(password)){
            throw new ExceptionUserIncomplete("La contraseña debe ser cargada!!");
        }
        //else if(!CheckPassword.verifyPassword(password)){
          //  throw new ExceptionUserIncorrect("La contraseña no es valida!!");
        //}

        */
        return new User(id,home,name,password);
    }
    public  void edit(String name, String password, String mail,Home home) {
        if(CkeckName.verifyLoadedUser(name)){
            if((!CkeckName.verifyUser(name))){
                throw  new ExceptionUserIncorrect("Ingrese un formato adecuado!!");
            }
            else{
                this.name=name;
            }
        }
        if(CheckEmail.verifyLoadedMail(mail)){
            if(!CheckEmail.verifyEmail(mail)){
                throw new ExceptionUserIncorrect("El correo electronico es invalido!!");
            }
            else{
                this.mail=mail;
            }
        }
        if(CheckPassword.verifyLoadedPass(password)){
            //if(!CheckPassword.verifyPassword(password)){
              //  throw new ExceptionUserIncorrect("La contraseña no es valida!!");
            //}
            //else {
                this.password=password;
            //}
        }
        this.home=home;
    }

    public Long getIdUser() {
        return idUser;
    }

    public void setIdUser(Long idUser) {
        this.idUser = idUser;
    }

    public Home getHome() {
        return home;
    }

    public void setHome(Home home) {
        this.home = home;
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
