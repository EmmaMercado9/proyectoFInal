package com.ema.practica.infrastructure.order.rest.model;

import com.ema.practica.infrastructure.moduloProduct.client.rest.model.ClientResumenDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class OrderListadoDTO {
    private Long idOrder;
    private UUID trackingId;
    private LocalDateTime orderDate;
    private String canal;
    private String payment;
    private String status;
    private BigDecimal totalAmount;
    private Integer cantidadItems;
    private ClientResumenDTO cliente;  // ← agregar

}

