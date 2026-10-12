package com.ema.practica.infrastructure.util.user.rest.model;

import lombok.Data;

import java.util.UUID;

@Data
public class UserResumenDTO {
    private UUID id;
    private String nombre;
    private String email;
}