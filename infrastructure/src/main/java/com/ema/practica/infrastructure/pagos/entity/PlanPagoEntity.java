package com.ema.practica.infrastructure.pagos.entity;

import com.ema.practica.infrastructure.order.enumOrder.PaymentMethod;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

/**
 * Ajuste de precio según cómo paga el cliente. El precio cargado en los productos es el
 * de lista (con tarjeta); cada plan suma o resta un porcentaje sobre el total de la venta:
 * negativo = descuento (ej. efectivo −10 %), positivo = recargo (ej. 6 cuotas +20 %).
 */
@Entity
@Table(name = "planes_pago",
        uniqueConstraints = @UniqueConstraint(name = "uk_plan_pago", columnNames = {"medio", "cuotas"}))
@Getter
@Setter
@NoArgsConstructor
public class PlanPagoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private PaymentMethod medio;

    /** 1 para todo lo que no es crédito; 1, 3, 4 o 6 para tarjeta de crédito. */
    @Column(nullable = false)
    private Integer cuotas = 1;

    /** Porcentaje sobre el total: negativo = descuento, positivo = recargo. */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentaje = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean activo = true;

    public PlanPagoEntity(PaymentMethod medio, int cuotas, String porcentaje) {
        this.medio = medio;
        this.cuotas = cuotas;
        this.porcentaje = new BigDecimal(porcentaje);
    }
}
