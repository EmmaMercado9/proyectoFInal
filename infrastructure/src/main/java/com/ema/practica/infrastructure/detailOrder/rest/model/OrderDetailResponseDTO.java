package com.ema.practica.infrastructure.detailOrder.rest.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderDetailResponseDTO {
    private Long idDetail;
    private Integer quantity;
    private Integer cantidadDevuelta;   // unidades ya devueltas
    private BigDecimal priceUnit;
    private BigDecimal subtotal;
    // Datos de la variante vendida
    private Long variantId;
    private String sku;
    private String productoNombre;
    private String talla;              // de variante_atributos
    private String color;
    private String imagenUrl;
}