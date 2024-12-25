package com.ema.practica.infrastructure.user.rest.model;

import com.ema.practica.core.home.model.Home;
import com.ema.practica.core.user.model.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@Data

public class UserDTO {

    private Long idUser;

   @JsonProperty(value="home")
    private Home home;
    @JsonProperty(value="role")
    private String role;
    @JsonProperty(value="name")
    private String name;
    @JsonProperty(value="mail")
    private String mail;
    @JsonProperty(value="password")
    private String password;

    public User dtoToCore() {
        return User.instance(idUser, home, name, mail, password);
    }

    public User dtoToCoreLoggin() {
        return User.instance(idUser, home, name, password);
    }

    // Método para convertir de Core a DTO
    public  static  UserDTO coreToDTO(User user) {
        return new UserDTO(
                user.getIdUser(),
                user.getHome(),
                user.getRole(),
                user.getName(),
                user.getMail(),
                user.getPassword()
        );
    }
}
