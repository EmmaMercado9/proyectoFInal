package com.ema.practica.infrastructure.order.rest.model;

import com.ema.practica.infrastructure.detailOrder.rest.model.OrderDetailRequestDTO;
import com.ema.practica.infrastructure.order.enumOrder.CanalVenta;
import com.ema.practica.infrastructure.order.enumOrder.PaymentMethod;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

// OrderRequestDTO.java — lo que llega desde el frontend
@Data
public class OrderRequestDTO {

    // null = venta a cliente anónimo (mostrador)
    private UUID userId;
    private UUID       clienteId;   // ← UUID, no Long


    // Lo asigna el controller según el endpoint; no se acepta del cliente
    private CanalVenta canal;              // ADMIN u ONLINE

    @NotNull
    private PaymentMethod payment;

    private String address;                // solo para ONLINE
    private String description;

    @DecimalMin(value = "0.00", message = "El descuento no puede ser negativo")
    private BigDecimal discount;           // descuento manual, puede ser null

    @NotEmpty(message = "La orden debe tener al menos un item")
    private List<@Valid OrderDetailRequestDTO> items;
}

