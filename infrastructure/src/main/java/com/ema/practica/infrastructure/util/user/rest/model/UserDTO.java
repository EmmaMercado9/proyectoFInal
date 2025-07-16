package com.ema.practica.infrastructure.user.rest.model;

import com.ema.practica.core.home.model.Home;
import com.ema.practica.core.user.model.User;
import com.ema.practica.infrastructure.user.entitty.UserEntity;
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

    @JsonProperty(value="role")
    private String role;
    @JsonProperty(value="name")
    private String name;
    @JsonProperty(value="mail")
    private String mail;
    @JsonProperty(value="password")
    private String password;

    public UserEntity dtoToEntity() {
        return new UserEntity(idUser,role, name, mail, password);
    }


    // Método para convertir de Core a DTO
    public  static  UserDTO entityToDTO(UserEntity user) {
        return new UserDTO(
                user.getIdUser(),
                user.getRole(),
                user.getName(),
                user.getMail(),
                user.getPassword()
        );
    }
}
