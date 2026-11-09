package com.ema.practica.infrastructure.moduloProduct.client.useCases;

import com.ema.practica.infrastructure.exception.VentaInvalidaException;
import com.ema.practica.infrastructure.moduloProduct.client.crud.ClientRepository;
import com.ema.practica.infrastructure.moduloProduct.client.entity.ClientEntity;
import com.ema.practica.infrastructure.moduloProduct.client.rest.model.ClientDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock ClientRepository clientRepository;
    @InjectMocks ClientService service;

    private static ClientDTO dto(String dni, String mail) {
        ClientDTO d = new ClientDTO();
        d.setName("Juan");
        d.setUsername("Pérez");
        d.setDni(dni);
        d.setMail(mail);
        return d;
    }

    @Test
    void crear_sinEmailNiDni_losGuardaVacios() {
        when(clientRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ClientDTO creado = service.crear(dto("  ", ""));

        assertThat(creado.getDni()).isNull();
        assertThat(creado.getMail()).isNull();
        verify(clientRepository, never()).existsByDni(any());
    }

    @Test
    void crear_conDniRepetido_falla() {
        when(clientRepository.existsByDni("30123456")).thenReturn(true);

        assertThatThrownBy(() -> service.crear(dto("30123456", null)))
                .isInstanceOf(VentaInvalidaException.class);
        verify(clientRepository, never()).save(any(ClientEntity.class));
    }
}
