package com.ema.practica.infrastructure.moduloProduct.atributos.rest.model;

import com.ema.practica.infrastructure.moduloProduct.atributos.entity.AtributoEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AtributoRequestDTO {

    @NotBlank
    private String nombre;

}