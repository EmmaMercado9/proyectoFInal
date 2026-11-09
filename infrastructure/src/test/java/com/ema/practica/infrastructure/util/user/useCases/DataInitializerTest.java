package com.ema.practica.infrastructure.util.user.useCases;

import com.ema.practica.infrastructure.util.rol.Rol;
import com.ema.practica.infrastructure.util.user.crud.ICreateUserCrud;
import com.ema.practica.infrastructure.util.user.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DataInitializerTest {

    @Mock ICreateUserCrud createUserCrud;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks DataInitializer initializer;

    private void configurar(String password) {
        ReflectionTestUtils.setField(initializer, "adminPassword", password);
        ReflectionTestUtils.setField(initializer, "adminEmail", "duenio@cloufit.test");
    }

    @Test
    void sinContrasenaConfigurada_noCreaElAdmin() {
        configurar("");
        when(createUserCrud.findByName("admin")).thenReturn(Optional.empty());

        initializer.run();

        verify(createUserCrud, never()).save(any());
    }

    @Test
    void conContrasenaConfigurada_creaElAdminConEsaClaveCifrada() {
        configurar("una-clave-larga-de-prueba");
        when(createUserCrud.findByName("admin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("una-clave-larga-de-prueba")).thenReturn("cifrada");

        initializer.run();

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(createUserCrud).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("cifrada");
        assertThat(captor.getValue().getMail()).isEqualTo("duenio@cloufit.test");
        assertThat(captor.getValue().getRol()).isEqualTo(Rol.ADMIN);
    }

    @Test
    void siElAdminYaExiste_noLoModifica() {
        configurar("una-clave-larga-de-prueba");
        when(createUserCrud.findByName("admin")).thenReturn(Optional.of(new UserEntity()));

        initializer.run();

        verify(createUserCrud, never()).save(any());
    }
}
