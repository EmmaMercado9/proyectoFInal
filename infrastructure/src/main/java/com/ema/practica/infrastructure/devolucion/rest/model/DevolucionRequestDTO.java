package com.ema.practica.infrastructure.devolucion.rest.model;

import com.ema.practica.infrastructure.detailOrder.rest.model.OrderDetailRequestDTO;
import com.ema.practica.infrastructure.devolucion.enums.FormaReintegro;
import com.ema.practica.infrastructure.devolucion.enums.TipoDevolucion;
import com.ema.practica.infrastructure.order.enumOrder.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

// POST /api/admin/orders/{idOrder}/devoluciones
@Data
public class DevolucionRequestDTO {

    @NotNull(message = "Indicá si es devolución o cambio")
    private TipoDevolucion tipo;

    @Size(max = 255, message = "El motivo no puede superar los 255 caracteres")
    private String motivo;

    // Obligatorio cuando hay dinero a reintegrar
    private FormaReintegro formaReintegro;

    // Solo CAMBIO: con qué paga el cliente si lo nuevo sale más caro
    private PaymentMethod paymentDiferencia;

    @NotEmpty(message = "Elegí al menos un producto a devolver")
    private List<@Valid DevolucionItemRequestDTO> items = new ArrayList<>();

    // Solo CAMBIO: productos que se lleva
    private List<@Valid OrderDetailRequestDTO> itemsNuevos = new ArrayList<>();
}
