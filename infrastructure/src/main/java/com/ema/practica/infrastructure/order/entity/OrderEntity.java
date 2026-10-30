package com.ema.practica.infrastructure.order.entity;
import com.ema.practica.infrastructure.detailOrder.entity.OrderDetailEntity;
import com.ema.practica.infrastructure.exception.VentaInvalidaException;
import com.ema.practica.infrastructure.moduloProduct.client.entity.ClientEntity;
import com.ema.practica.infrastructure.order.enumOrder.CanalVenta;
import com.ema.practica.infrastructure.order.enumOrder.OrderStatus;
import com.ema.practica.infrastructure.order.enumOrder.PaymentMethod;
import com.ema.practica.infrastructure.util.user.entity.UserEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_order")
    private Long idOrder;

    // UUID público — se muestra al cliente, nunca el id_order secuencial
    @Column(name = "tracking_id", unique = true, nullable = false, updatable = false)
    private UUID trackingId = UUID.randomUUID();

    // null = venta de mostrador sin usuario registrado
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_client")
    private ClientEntity clientEntity;


    // Canal de venta — ADMIN (punto de venta físico) u ONLINE (tienda web)
    @Enumerated(EnumType.STRING)
    @Column(name = "canal", nullable = false)
    private CanalVenta canal;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment", nullable = false)
    private PaymentMethod payment;  // enum: EFECTIVO, TARJETA, TRANSFERENCIA, MERCADOPAGO

    // VARCHAR (no ENUM de MySQL) para poder agregar estados sin migrar la columna
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "status", nullable = false, length = 30)
    private OrderStatus status = OrderStatus.PENDIENTE;

    // ── Cancelación ─────────────────────────────────────────────────────────
    @Column(name = "motivo_cancelacion")
    private String motivoCancelacion;

    @Column(name = "fecha_cancelacion")
    private LocalDateTime fechaCancelacion;

    @Column(name = "cancelado_por")
    private String canceladoPor;

    // LocalDateTime — necesitamos la hora, no solo el día
    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate = LocalDateTime.now();

    // Todos BigDecimal — nunca double ni Long para dinero
    @Column(name = "subtotal", precision = 10, scale = 2)
    private BigDecimal subtotal;           // suma de todos los detalles

    @Column(name = "discount", precision = 10, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    // ── Medio de pago: descuento (efectivo) o recargo (cuotas) ───────────────
    // Ventas anteriores a esta columna quedan en null = 1 cuota, sin ajuste
    @Column(name = "cuotas")
    private Integer cuotas = 1;

    // Porcentaje aplicado: negativo = descuento, positivo = recargo
    @Column(name = "ajuste_porcentaje", precision = 5, scale = 2)
    private BigDecimal ajustePorcentaje = BigDecimal.ZERO;

    // Importe resultante del porcentaje (negativo = descuento)
    @Column(name = "ajuste_medio_pago", precision = 10, scale = 2)
    private BigDecimal ajusteMedioPago = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;        // subtotal - discount + ajusteMedioPago

    // Opcional: dirección de envío (para canal ONLINE)
    @Column(name = "address")
    private String address;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetailEntity> details = new ArrayList<>();

    // Método de negocio: recalcula totales desde los detalles
    public void recalcularTotales() {
        this.subtotal = details.stream()
                .map(OrderDetailEntity::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal desc = this.discount != null ? this.discount : BigDecimal.ZERO;
        if (desc.signum() < 0 || desc.compareTo(this.subtotal) > 0) {
            throw new VentaInvalidaException(
                    "El descuento debe estar entre 0 y el subtotal (" + this.subtotal + ")");
        }
        BigDecimal base = this.subtotal.subtract(desc);

        // El porcentaje del medio de pago se aplica después del descuento manual
        BigDecimal pct = this.ajustePorcentaje != null ? this.ajustePorcentaje : BigDecimal.ZERO;
        this.ajusteMedioPago = base.multiply(pct)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        this.totalAmount = base.add(this.ajusteMedioPago);
    }

    public int getCuotasSeguras() {
        return cuotas != null && cuotas > 0 ? cuotas : 1;
    }

    public BigDecimal getAjusteMedioPagoSeguro() {
        return ajusteMedioPago != null ? ajusteMedioPago : BigDecimal.ZERO;
    }
}