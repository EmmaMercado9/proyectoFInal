package com.ema.practica.infrastructure.moduloProduct.client.useCases;

import com.ema.practica.infrastructure.exception.RecursoNoEncontradoException;
import com.ema.practica.infrastructure.exception.VentaInvalidaException;
import com.ema.practica.infrastructure.moduloProduct.client.crud.ClientRepository;
import com.ema.practica.infrastructure.moduloProduct.client.entity.ClientEntity;
import com.ema.practica.infrastructure.moduloProduct.client.rest.model.ClientDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    // ─── Buscar por nombre, DNI, email o username ─────────────────────────
    public List<ClientDTO> buscar(String q) {
        List<ClientEntity> clientes = (q == null || q.isBlank())
                ? clientRepository.findAll()
                : clientRepository.buscar(q);

        return clientes.stream()
                .map(this::toDTO)
                .toList();
    }
    public List<ClientDTO> all() {
        List<ClientEntity> clientes = clientRepository.findAll();
        if (clientes == null) return List.of();
        return clientes.stream().map(this::toDTO).toList();
    }
    // ─── Crear cliente ────────────────────────────────────────────────────
    @Transactional
    public ClientDTO crear(ClientDTO dto) {
        // Verificar si ya existe por DNI
        if (dto.getDni() != null && !dto.getDni().isBlank()) {
            boolean existe = clientRepository.existsByDni(dto.getDni());
            if (existe) {
                throw new VentaInvalidaException(
                        "Ya existe un cliente con DNI " + dto.getDni()
                );
            }
        }

        ClientEntity cliente = new ClientEntity();
        cliente.setName(dto.getName());
        cliente.setUsername(dto.getUsername());
        cliente.setMail(dto.getMail());
        cliente.setDni(dto.getDni());

        ClientEntity guardado = clientRepository.save(cliente);
        return toDTO(guardado);
    }

    // ─── Obtener por ID ───────────────────────────────────────────────────
    public ClientDTO getById(UUID id) {
        ClientEntity cliente = clientRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Cliente no encontrado: " + id
                ));
        return toDTO(cliente);
    }

    // ─── Mapper ───────────────────────────────────────────────────────────
    private ClientDTO toDTO(ClientEntity c) {
        ClientDTO dto = new ClientDTO();
        dto.setIdClient(c.getIdClient());
        dto.setName(c.getName());
        dto.setUsername(c.getUsername());
        dto.setMail(c.getMail());
        dto.setDni(c.getDni());
        return dto;
    }
}