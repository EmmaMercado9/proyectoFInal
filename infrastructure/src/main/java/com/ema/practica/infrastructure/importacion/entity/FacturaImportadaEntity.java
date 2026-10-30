package com.ema.practica.infrastructure.importacion.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Registro mínimo de las facturas de proveedores ya importadas.
 * Su único fin es impedir que la misma factura se cargue dos veces
 * (lo que duplicaría el stock): CUIT + número es único.
 */
@Entity
@Table(name = "facturas_importadas",
        uniqueConstraints = @UniqueConstraint(name = "uk_factura_importada", columnNames = {"cuit", "numero"}))
@Getter
@Setter
@NoArgsConstructor
public class FacturaImportadaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 13)
    private String cuit;

    @Column(nullable = false, length = 120)
    private String proveedor;

    @Column(nullable = false, length = 20)
    private String numero;

    private LocalDate fecha;

    @Column(precision = 14, scale = 2)
    private BigDecimal total;

    private Integer unidades;

    private String usuario;

    private LocalDateTime importadaEn;
}
