package com.ema.practica.infrastructure.order.rest.model;

import com.ema.practica.infrastructure.detailOrder.rest.model.OrderDetailResponseDTO;
import com.ema.practica.infrastructure.moduloProduct.client.rest.model.ClientResumenDTO;
import com.ema.practica.infrastructure.util.user.rest.model.UserResumenDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class OrderResponseDTO {
    private Long idOrder;
    private UUID trackingId;

    private LocalDateTime orderDate;
    private String canal;
    private String payment;
    private String status;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal totalAmount;
    private String address;
    private String description;

    // Cancelación
    private String motivoCancelacion;
    private LocalDateTime fechaCancelacion;
    private String canceladoPor;

    // Hasta cuándo se aceptan devoluciones o cambios
    private LocalDateTime fechaLimiteDevolucion;
    private UserResumenDTO user;
    private List<OrderDetailResponseDTO> details;
    private ClientResumenDTO cliente;  // ← agregar

}