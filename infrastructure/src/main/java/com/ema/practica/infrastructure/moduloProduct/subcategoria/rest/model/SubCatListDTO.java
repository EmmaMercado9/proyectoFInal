package com.ema.practica.infrastructure.moduloProduct.subcategoria.rest.model;

import com.ema.practica.infrastructure.moduloProduct.subcategoria.entity.SubCategoriaEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubCatListDTO {
    @JsonProperty(value = "nombre")
    private String nombre;

    public  static SubCatListDTO entityToDto(SubCategoriaEntity subCategoriaEntity){
        return new SubCatListDTO(subCategoriaEntity.getNombre());
    }
}
