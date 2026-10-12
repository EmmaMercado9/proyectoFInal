package com.ema.practica.infrastructure.facturacion.rest.model;

import lombok.Data;

// Todo opcional: sin datos se factura a consumidor final (o al DNI del cliente de la venta)
@Data
public class FacturarRequestDTO {
    private Integer docTipo;                 // 80 CUIT, 86 CUIL, 96 DNI, 99 sin identificar
    private Long docNro;
    private Integer condicionIvaReceptorId;  // 5 consumidor final, 1 RI, 6 monotributo, 4 exento...
}
