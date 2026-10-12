package com.ema.practica.infrastructure.facturacion.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Datos del emisor para facturar, editables por ADMIN desde el panel.
 * Hay una sola fila (id = 1). El certificado y la clave viven como archivos
 * en el servidor (ver ConfiguracionArcaService), nunca en la base.
 */
@Entity
@Table(name = "config_facturacion")
@Getter
@Setter
@NoArgsConstructor
public class ConfiguracionFacturacionEntity {

    public static final long ID = 1L;

    @Id
    private Long id = ID;

    @Column(name = "habilitada", nullable = false)
    private boolean habilitada = false;

    @Column(name = "cuit")
    private Long cuit;

    @Column(name = "punto_venta")
    private Integer puntoVenta;

    @Column(name = "razon_social")
    private String razonSocial;

    @Column(name = "domicilio")
    private String domicilio;

    @Column(name = "iibb", length = 30)
    private String iibb;

    @Column(name = "inicio_actividades", length = 10)
    private String inicioActividades;      // dd/MM/yyyy

    @Column(name = "condicion_iva", length = 60)
    private String condicionIva = "Responsable Monotributo";

    @Column(name = "actualizado_por")
    private String actualizadoPor;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;
}
