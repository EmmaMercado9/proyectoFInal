package com.ema.practica.infrastructure.facturacion.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// Códigos de la tabla de comprobantes de ARCA (FEParamGetTiposCbte)
@Getter
@RequiredArgsConstructor
public enum TipoComprobante {
    FACTURA_C(11, "Factura C"),
    NOTA_CREDITO_C(13, "Nota de Crédito C");

    private final int codigo;
    private final String descripcion;
}
