package com.ema.practica.infrastructure.order.rest.model;

import com.ema.practica.infrastructure.detailOrder.rest.model.OrderDetailRequestDTO;
import com.ema.practica.infrastructure.order.enumOrder.CanalVenta;
import com.ema.practica.infrastructure.order.enumOrder.PaymentMethod;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    // Solo para TARJETA_CREDITO (1, 3, 4, 6...); null = 1 cuota
    @Min(value = 1, message = "Las cuotas deben ser al menos 1")
    private Integer cuotas;

    // Pago combinado: dos o más medios. Si viene, reemplaza a payment y cuotas
    @Size(max = 5, message = "Se pueden combinar hasta 5 pagos")
    private List<@Valid PagoRequestDTO> pagos;

    // Uso interno (venta de un cambio): mantiene el porcentaje de la venta original.
    // No se acepta del frontend: el porcentaje siempre lo decide el servidor.
    @JsonIgnore
    private BigDecimal ajustePorcentajeForzado;

    @NotEmpty(message = "La orden debe tener al menos un item")
    private List<@Valid OrderDetailRequestDTO> items;
}

