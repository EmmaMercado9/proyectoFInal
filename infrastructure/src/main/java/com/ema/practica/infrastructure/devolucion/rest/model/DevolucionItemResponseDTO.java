package com.ema.practica.infrastructure.devolucion.rest.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DevolucionItemResponseDTO {
    private Long idDetail;
    private Long variantId;
    private String sku;
    private String productoNombre;
    private String variante;          // "Talla 42 / Color Negro"
    private Integer quantity;
    private Boolean reingresaStock;
    private BigDecimal monto;
}
