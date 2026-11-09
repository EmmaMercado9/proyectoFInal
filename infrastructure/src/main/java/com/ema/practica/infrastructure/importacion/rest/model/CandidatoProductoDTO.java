package com.ema.practica.infrastructure.importacion.rest.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Producto del catálogo al que se puede asociar un renglón de la factura. */
public record CandidatoProductoDTO(UUID id, String nombre, String sku, BigDecimal precioVenta,
                                   boolean sinTalles, List<VarianteExistente> variantes) {

    public record VarianteExistente(Long varianteId, String talle, Integer stock) {
    }
}
