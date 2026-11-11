package com.ema.practica.infrastructure.moduloProduct.client.useCases;

import com.ema.practica.infrastructure.exception.VentaInvalidaException;
import com.ema.practica.infrastructure.moduloProduct.client.crud.ClientRepository;
import com.ema.practica.infrastructure.moduloProduct.client.entity.ClientEntity;
import com.ema.practica.infrastructure.moduloProduct.client.rest.model.ClientDTO;
import com.ema.practica.infrastructure.order.crud.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock ClientRepository clientRepository;
    @Mock OrderRepository orderRepository;
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

    private ClientEntity existente(UUID id) {
        ClientEntity c = new ClientEntity();
        c.setIdClient(id);
        c.setName("Juan");
        c.setDni("30123456");
        return c;
    }

    @Test
    void actualizar_cambiaDatosYVaciaLosOpcionales() {
        UUID id = UUID.randomUUID();
        ClientEntity c = existente(id);
        when(clientRepository.findById(id)).thenReturn(Optional.of(c));

        ClientDTO editado = service.actualizar(id, dto("", "juan@mail.com"));

        assertThat(editado.getName()).isEqualTo("Juan");
        assertThat(c.getDni()).isNull();
        assertThat(c.getMail()).isEqualTo("juan@mail.com");
    }

    @Test
    void actualizar_conDniDeOtroCliente_falla() {
        UUID id = UUID.randomUUID();
        when(clientRepository.findById(id)).thenReturn(Optional.of(existente(id)));
        when(clientRepository.existsByDniAndIdClientNot("40111222", id)).thenReturn(true);

        assertThatThrownBy(() -> service.actualizar(id, dto("40111222", null)))
                .isInstanceOf(VentaInvalidaException.class);
    }

    @Test
    void eliminar_sinVentas_loBorra() {
        UUID id = UUID.randomUUID();
        ClientEntity c = existente(id);
        when(clientRepository.findById(id)).thenReturn(Optional.of(c));
        when(orderRepository.existsByClientEntity_IdClient(id)).thenReturn(false);

        service.eliminar(id);

        verify(clientRepository).delete(c);
    }

    @Test
    void eliminar_conVentas_falla() {
        UUID id = UUID.randomUUID();
        when(clientRepository.findById(id)).thenReturn(Optional.of(existente(id)));
        when(orderRepository.existsByClientEntity_IdClient(id)).thenReturn(true);

        assertThatThrownBy(() -> service.eliminar(id))
                .isInstanceOf(VentaInvalidaException.class)
                .hasMessageContaining("tiene ventas");
        verify(clientRepository, never()).delete(any(ClientEntity.class));
    }

    @Test
    void crear_conDniRepetido_falla() {
        when(clientRepository.existsByDni("30123456")).thenReturn(true);

        assertThatThrownBy(() -> service.crear(dto("30123456", null)))
                .isInstanceOf(VentaInvalidaException.class);
        verify(clientRepository, never()).save(any(ClientEntity.class));
    }
}
