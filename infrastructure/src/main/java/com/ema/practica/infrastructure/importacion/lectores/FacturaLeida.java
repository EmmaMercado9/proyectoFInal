package com.ema.practica.infrastructure.importacion.lectores;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Resultado de leer una factura de proveedor: encabezado + renglones. */
public record FacturaLeida(String proveedor, String cuit, String numero, LocalDate fecha,
                           BigDecimal total, List<LineaFactura> lineas) {

    /** Suma de los importes de los renglones (debe coincidir con el total de la factura). */
    public BigDecimal sumaLineas() {
        return lineas.stream().map(LineaFactura::importe).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
