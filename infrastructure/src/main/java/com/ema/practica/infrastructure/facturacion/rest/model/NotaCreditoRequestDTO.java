package com.ema.practica.infrastructure.facturacion.rest.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * - con devolucionId: por el valor de esa devolución
 * - con importe: parcial por ese monto
 * - sin nada: por todo el saldo de la factura (anulación total)
 */
@Data
public class NotaCreditoRequestDTO {
    private Long devolucionId;

    @DecimalMin(value = "0.01", message = "El importe debe ser mayor a 0")
    private BigDecimal importe;

    @Size(max = 255, message = "El motivo no puede superar los 255 caracteres")
    private String motivo;
}
