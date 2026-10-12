package com.ema.practica.infrastructure.moduloProduct.marcas.rest.model;

import com.ema.practica.infrastructure.moduloProduct.marcas.entity.MarcaEntity;
import com.ema.practica.infrastructure.util.status.StatusEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarcaDto {
    private Long marcaId;
    @JsonProperty(value = "nombre")
    private String nombre;
    @JsonProperty(value = "status")
    private String status;

    public MarcaEntity dtoToEntity(){
        return new MarcaEntity(marcaId,nombre,StatusEntity.valueOf(status.toUpperCase()));
    }
    public static MarcaDto entityToDto(MarcaEntity marcaEntity){
        return new MarcaDto(marcaEntity.getMarcaId(),marcaEntity.getNombre(),marcaEntity.getStatus().name());
    }
}
