package com.ema.practica.infrastructure.moduloProduct.atributos.rest.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AtributoResponseDTO {

    private Long atributoId;
    private String nombre;

}