package com.ema.practica.infrastructure.order.rest.model;

import jakarta.validation.constraints.Size;
import lombok.Data;

// Body opcional de PATCH /api/admin/orders/{id}/cancelar
@Data
public class CancelarOrdenDTO {
    @Size(max = 255, message = "El motivo no puede superar los 255 caracteres")
    private String motivo;
}
