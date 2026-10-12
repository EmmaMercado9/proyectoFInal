package com.ema.practica.infrastructure.moduloProduct.client.rest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientResumenDTO {
    private UUID idClient;
    private String name;
    private String mail;
    private String dni;
}