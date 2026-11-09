package com.ema.practica.infrastructure.order.entity;

import com.ema.practica.infrastructure.order.enumOrder.PaymentMethod;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

/** Una parte de una venta pagada con más de un medio de pago. */
@Entity
@Table(name = "pagos_venta")
@Getter
@Setter
@NoArgsConstructor
public class PagoVentaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_order")
    private OrderEntity order;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private PaymentMethod medio;

    @Column(nullable = false)
    private Integer cuotas = 1;

    /** Parte del precio que cubre este pago, antes del descuento o recargo del medio. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentaje = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal ajuste = BigDecimal.ZERO;

    public PagoVentaEntity(OrderEntity order, PaymentMethod medio, int cuotas, BigDecimal monto, BigDecimal porcentaje) {
        this.order = order;
        this.medio = medio;
        this.cuotas = cuotas;
        this.monto = monto;
        this.porcentaje = porcentaje != null ? porcentaje : BigDecimal.ZERO;
    }

    /** Lo que se cobra con este medio. */
    public BigDecimal getTotal() {
        return monto.add(ajuste);
    }
}
