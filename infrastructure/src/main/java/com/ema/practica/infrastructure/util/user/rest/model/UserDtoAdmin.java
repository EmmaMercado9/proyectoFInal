package com.ema.practica.infrastructure.util.user.rest.model;

import com.ema.practica.infrastructure.util.rol.Rol;
import com.ema.practica.infrastructure.util.status.StatusEntity;
import com.ema.practica.infrastructure.util.user.entity.UserEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDtoAdmin {
    private UUID uuid;
    private String role;
    private String name;
    private String mail;
    private String password;
    private String status;
    private String createdBy;
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;
    public static  UserDtoAdmin  EntityToUserAdmin(UserEntity userEntity){
        return new UserDtoAdmin(userEntity.getIdUser(),userEntity.getRol().name(), userEntity.getName(), userEntity.getMail(),null /* nunca exponer el hash */,userEntity.getStatus().name(),userEntity.getCreatedBy(),userEntity.getCreatedDate(),userEntity.getLastModifiedBy(),userEntity.getLastModifiedDate());
    }
}


