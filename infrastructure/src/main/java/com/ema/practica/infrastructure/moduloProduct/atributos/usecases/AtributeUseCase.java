package com.ema.practica.infrastructure.moduloProduct.atributos.usecases;


import com.ema.practica.infrastructure.exception.VentaInvalidaException;
import com.ema.practica.infrastructure.moduloProduct.atributos.crud.AtributoRepository;
import com.ema.practica.infrastructure.moduloProduct.atributos.entity.AtributoEntity;
import com.ema.practica.infrastructure.moduloProduct.atributos.rest.model.AtributoRequestDTO;
import com.ema.practica.infrastructure.moduloProduct.atributos.rest.model.AtributoResponseDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AtributeUseCase {

    private final AtributoRepository atributoRepository;


    @Transactional
    public AtributoResponseDTO crearAtributo(AtributoRequestDTO dto) {
        if (atributoRepository.existsByNombreIgnoreCase(
                dto.getNombre()
                )){

            throw new VentaInvalidaException("Ese atributo ya existe");
        }

        AtributoEntity atributo = new AtributoEntity();
        atributo.setNombre(dto.getNombre());

        atributo = atributoRepository.save(atributo);

        return new AtributoResponseDTO(
                atributo.getAtributoId(),
                atributo.getNombre()
        );
    }

    public List<AtributoResponseDTO> all() {
        return atributoRepository.findAll().stream().map(this::toResponse).toList();
    }

    public AtributoResponseDTO toResponse(AtributoEntity atributo){
        return new AtributoResponseDTO(atributo.getAtributoId(),atributo.getNombre());
    }
}