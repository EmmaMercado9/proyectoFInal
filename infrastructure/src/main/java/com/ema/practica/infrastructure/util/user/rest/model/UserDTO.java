package com.ema.practica.infrastructure.util.user.rest.model;

import com.ema.practica.infrastructure.util.rol.Rol;
import com.ema.practica.infrastructure.util.status.StatusEntity;
import com.ema.practica.infrastructure.util.user.entity.UserEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@Data

public class UserDTO {

    private UUID idUser;
    @JsonProperty(value="role")
    private String role;
    @JsonProperty(value="name")
    private String name;
    @JsonProperty(value="mail")
    private String mail;
    @JsonProperty(value="password")
    private String password;
    @JsonProperty(value="status")
    private String status;
    public UserEntity dtoToEntity() {
        return new UserEntity(idUser, Rol.valueOf(role.toUpperCase()), name, mail, password,StatusEntity.valueOf(status.toUpperCase()));
    }


    // Método para convertir de Core a DTO
    public  static  UserDTO entityToDTO(UserEntity user) {
        return new UserDTO(
                user.getIdUser(),
                user.getRol().name(),
                user.getName(),
                user.getMail(),
                null, // nunca exponer el hash
                user.getStatus().name()

        );
    }
}
