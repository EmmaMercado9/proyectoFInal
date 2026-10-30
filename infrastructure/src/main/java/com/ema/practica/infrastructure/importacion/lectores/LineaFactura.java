package com.ema.practica.infrastructure.importacion.lectores;

import java.math.BigDecimal;

/**
 * Un renglón de la factura del proveedor, ya en el formato común.
 * Todos los lectores devuelven esto, sea cual sea el formato del PDF.
 *
 * @param codigoArticulo código del modelo (agrupa los talles en un producto)
 * @param skuVariante    código del talle puntual (SKU de la variante)
 * @param costoUnitario  lo que se pagó por unidad, con descuento e IVA incluidos
 * @param precioLista    precio de lista del proveedor, si la factura lo trae (puede ser null)
 */
public record LineaFactura(String codigoArticulo, String descripcion, String marca, String talle,
                           String skuVariante, int cantidad, BigDecimal costoUnitario,
                           BigDecimal precioLista, BigDecimal importe) {
}
