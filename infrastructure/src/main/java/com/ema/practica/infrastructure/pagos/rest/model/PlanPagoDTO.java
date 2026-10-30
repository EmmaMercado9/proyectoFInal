package com.ema.practica.infrastructure.pagos.rest.model;

import com.ema.practica.infrastructure.order.enumOrder.PaymentMethod;
import com.ema.practica.infrastructure.pagos.entity.PlanPagoEntity;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** Plan de pago: porcentaje negativo = descuento, positivo = recargo. */
public record PlanPagoDTO(
        @NotNull PaymentMethod medio,
        @NotNull @Min(1) @Max(24) Integer cuotas,
        @NotNull @DecimalMin("-50.00") @DecimalMax("100.00") BigDecimal porcentaje,
        boolean activo) {

    public static PlanPagoDTO de(PlanPagoEntity e) {
        return new PlanPagoDTO(e.getMedio(), e.getCuotas(), e.getPorcentaje(), e.isActivo());
    }
}
