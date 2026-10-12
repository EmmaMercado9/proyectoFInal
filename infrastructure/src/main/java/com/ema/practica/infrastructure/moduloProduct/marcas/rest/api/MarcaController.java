package com.ema.practica.infrastructure.moduloProduct.marcas.rest.api;

import com.ema.practica.infrastructure.moduloProduct.marcas.rest.model.MarcaDto;
import com.ema.practica.infrastructure.moduloProduct.marcas.usecases.MarcaUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/marca")
public class MarcaController {

    private final MarcaUseCase marcaUseCase;
    @Autowired
    public MarcaController(MarcaUseCase marcaUseCase){
        this.marcaUseCase=marcaUseCase;
    }

    @PostMapping(value = "/marcaAdd")
    public ResponseEntity<?> createMarca(@RequestBody MarcaDto marcaDto){

        boolean result=marcaUseCase.createMarca(marcaDto);
        if(result) {
            return ResponseEntity.status(HttpStatus.CREATED).body("Creado correctamente");
        }
        else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocurrio un Error: ");

        }

    }

    @DeleteMapping(value = "/delete{marcaId}")
    public ResponseEntity<?> deleteMarca(@PathVariable Long marcaId){

        boolean result=marcaUseCase.deleteMarca(marcaId);
        if(result) {
            return ResponseEntity.status(HttpStatus.CREATED).body("Eliminado correctamente");
        }
        else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No se encontro");

        }

    }
    @GetMapping(value = "/all")
    public ResponseEntity<List<MarcaDto>> allMarca(){

        List<MarcaDto> marcaDtos =marcaUseCase.getAll();
        if(!marcaDtos.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(marcaDtos.stream().toList());
        }
        else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

        }

    }
    @PutMapping (value = "/modify")
    public ResponseEntity<?> modifyMarca(@RequestParam String nombre,@RequestParam Long id ){

        boolean result=marcaUseCase.modifyMarca(nombre,id);
        if(result) {
            return ResponseEntity.status(HttpStatus.CREATED).body("Modifico correctamente");
        }
        else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocurrio un error se encontro");

        }

    }
}
