package com.ema.practica.infrastructure.order.rest.model;

import com.ema.practica.infrastructure.order.enumOrder.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** Una parte de un pago combinado; el porcentaje lo decide siempre el servidor. */
public record PagoRequestDTO(
        @NotNull PaymentMethod medio,
        @Min(1) Integer cuotas,
        @NotNull @DecimalMin("0.01") BigDecimal monto) {
}
