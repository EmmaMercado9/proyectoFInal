package com.ema.practica.infrastructure.devolucion.rest.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DevolucionItemRequestDTO {
    // idDetail del renglón de la venta original
    @NotNull
    private Long idDetail;

    @NotNull
    @Min(value = 1, message = "La cantidad a devolver debe ser al menos 1")
    private Integer quantity;

    // false = producto fallado, no vuelve al stock
    private Boolean reingresaStock = true;
}
