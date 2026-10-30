package com.ema.practica.infrastructure.importacion.rest.model;

import com.ema.practica.infrastructure.importacion.entity.FacturaImportadaEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Una factura de proveedor ya importada (historial). */
public record FacturaImportadaDTO(Long id, String proveedor, String cuit, String numero, LocalDate fecha,
                                  BigDecimal total, Integer unidades, String usuario, LocalDateTime importadaEn) {

    public static FacturaImportadaDTO de(FacturaImportadaEntity e) {
        return new FacturaImportadaDTO(e.getId(), e.getProveedor(), e.getCuit(), e.getNumero(), e.getFecha(),
                e.getTotal(), e.getUnidades(), e.getUsuario(), e.getImportadaEn());
    }
}
