package com.ema.practica.infrastructure.importacion.rest.model;

/** Qué hizo la importación, para mostrarlo al usuario. */
public record ResultadoImportacionDTO(int productosCreados, int variantesCreadas,
                                      int variantesConStockSumado, int unidades) {
}
