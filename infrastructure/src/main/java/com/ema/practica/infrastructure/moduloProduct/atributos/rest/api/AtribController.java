package com.ema.practica.infrastructure.moduloProduct.atributos.rest.api;


import com.ema.practica.infrastructure.moduloProduct.atributos.rest.model.AtributoRequestDTO;
import com.ema.practica.infrastructure.moduloProduct.atributos.rest.model.AtributoResponseDTO;
import com.ema.practica.infrastructure.moduloProduct.atributos.usecases.AtributeUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/atributos")
@RequiredArgsConstructor
public class AtribController {

    private final AtributeUseCase atributeUseCase;



    @PostMapping
    public ResponseEntity<AtributoResponseDTO> crearAtributo(
            @RequestBody @Valid AtributoRequestDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(atributeUseCase.crearAtributo(dto));
    }
    @GetMapping
    public ResponseEntity<List<AtributoResponseDTO>> all() {
        return ResponseEntity.ok(
                (atributeUseCase.all()));
    }
}