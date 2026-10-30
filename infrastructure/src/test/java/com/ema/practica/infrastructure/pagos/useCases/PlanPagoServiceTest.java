package com.ema.practica.infrastructure.pagos.useCases;

import com.ema.practica.infrastructure.exception.VentaInvalidaException;
import com.ema.practica.infrastructure.order.enumOrder.PaymentMethod;
import com.ema.practica.infrastructure.pagos.crud.PlanPagoRepository;
import com.ema.practica.infrastructure.pagos.entity.PlanPagoEntity;
import com.ema.practica.infrastructure.pagos.rest.model.PlanPagoDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanPagoServiceTest {

    @Mock PlanPagoRepository repository;
    @InjectMocks PlanPagoService service;

    @Test
    void porcentajePara_devuelveElDelPlanConfigurado() {
        when(repository.findByMedioAndCuotas(PaymentMethod.TARJETA_CREDITO, 6))
                .thenReturn(Optional.of(new PlanPagoEntity(PaymentMethod.TARJETA_CREDITO, 6, "18.50")));

        assertThat(service.porcentajePara(PaymentMethod.TARJETA_CREDITO, 6)).isEqualByComparingTo("18.50");
    }

    @Test
    void porcentajePara_sinConfigurar_usaElValorPorDefecto() {
        when(repository.findByMedioAndCuotas(any(), anyInt())).thenReturn(Optional.empty());

        assertThat(service.porcentajePara(PaymentMethod.EFECTIVO, null)).isEqualByComparingTo("-10");
        assertThat(service.porcentajePara(PaymentMethod.TARJETA_DEBITO, 1)).isEqualByComparingTo("0");
    }

    @Test
    void porcentajePara_cuotasQueNoExisten_falla() {
        when(repository.findByMedioAndCuotas(any(), anyInt())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.porcentajePara(PaymentMethod.TARJETA_CREDITO, 12))
                .isInstanceOf(VentaInvalidaException.class)
                .hasMessageContaining("12 cuotas");
    }

    @Test
    void porcentajePara_planDesactivado_falla() {
        PlanPagoEntity plan = new PlanPagoEntity(PaymentMethod.TARJETA_CREDITO, 4, "10");
        plan.setActivo(false);
        when(repository.findByMedioAndCuotas(PaymentMethod.TARJETA_CREDITO, 4)).thenReturn(Optional.of(plan));

        assertThatThrownBy(() -> service.porcentajePara(PaymentMethod.TARJETA_CREDITO, 4))
                .isInstanceOf(VentaInvalidaException.class)
                .hasMessageContaining("no está habilitado");
    }

    @Test
    void soloLaTarjetaDeCreditoAceptaCuotas() {
        assertThatThrownBy(() -> service.porcentajePara(PaymentMethod.TARJETA_DEBITO, 3))
                .isInstanceOf(VentaInvalidaException.class);
        verifyNoInteractions(repository);
    }

    @Test
    void listar_creaLosPlanesQueFaltan() {
        when(repository.findByMedioAndCuotas(any(), anyInt())).thenReturn(Optional.empty());
        when(repository.findAllByOrderByMedioAscCuotasAsc()).thenReturn(List.of());

        service.listar();

        verify(repository, times(PlanPagoService.POR_DEFECTO.size())).save(any());
    }

    @Test
    void guardar_actualizaElPorcentaje() {
        PlanPagoEntity plan = new PlanPagoEntity(PaymentMethod.TARJETA_CREDITO, 3, "0");
        when(repository.findByMedioAndCuotas(any(), anyInt())).thenReturn(Optional.of(plan));
        when(repository.findAllByOrderByMedioAscCuotasAsc()).thenReturn(List.of(plan));

        service.guardar(List.of(new PlanPagoDTO(PaymentMethod.TARJETA_CREDITO, 3, new BigDecimal("8"), true)));

        assertThat(plan.getPorcentaje()).isEqualByComparingTo("8");
    }
}
