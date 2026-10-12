package com.ema.practica.infrastructure.devolucion.rest.model;

import com.ema.practica.infrastructure.moduloProduct.client.rest.model.ClientResumenDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class DevolucionResponseDTO {
    private Long idDevolucion;
    private String tipo;              // DEVOLUCION | CAMBIO
    private LocalDateTime fecha;
    private String usuario;           // quién la registró
    private String motivo;
    private String formaReintegro;    // EFECTIVO | MISMO_MEDIO | null

    // Venta original
    private Long idOrder;
    private UUID trackingIdOrden;
    private String paymentOrden;
    private ClientResumenDTO cliente;

    private BigDecimal montoDevuelto;
    private BigDecimal montoNuevaVenta;
    private BigDecimal diferencia;    // > 0 paga el cliente, < 0 se le reintegra

    // Venta nueva (solo CAMBIO)
    private Long idOrdenCambio;
    private UUID trackingIdOrdenCambio;

    private List<DevolucionItemResponseDTO> items;
}
