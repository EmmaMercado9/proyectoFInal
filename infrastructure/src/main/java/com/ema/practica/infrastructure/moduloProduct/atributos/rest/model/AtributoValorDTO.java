package com.ema.practica.infrastructure.moduloProduct.atributos.rest.model;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class AtributoValorDTO {
    @NotNull
    private Long atributoId;
    @NotBlank
    private String valor;
}
