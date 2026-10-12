package com.ema.practica.infrastructure.moduloProduct.client.rest.api;

import com.ema.practica.infrastructure.moduloProduct.client.rest.model.ClientDTO;
import com.ema.practica.infrastructure.moduloProduct.client.useCases.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;  // ← service, no repository

    @GetMapping
    public ResponseEntity<List<ClientDTO>> buscar(
            @RequestParam(defaultValue = "") String nombre) {
        return ResponseEntity.ok(clientService.buscar(nombre));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ClientDTO>> all()  {


        return ResponseEntity.ok(clientService.all());
    }

    @PostMapping
    public ResponseEntity<ClientDTO> crear(@RequestBody ClientDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clientService.crear(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(clientService.getById(id));
    }
}