package com.ema.practica.infrastructure.devolucion.entity;

import com.ema.practica.infrastructure.devolucion.enums.FormaReintegro;
import com.ema.practica.infrastructure.devolucion.enums.TipoDevolucion;
import com.ema.practica.infrastructure.order.entity.OrderEntity;
import com.ema.practica.infrastructure.util.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Devolución o cambio sobre una venta. Una venta puede tener varias.
@Entity
@Table(name = "devoluciones")
@Getter
@Setter
@NoArgsConstructor
public class DevolucionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_devolucion")
    private Long idDevolucion;

    // Venta original
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_order", nullable = false)
    private OrderEntity order;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoDevolucion tipo;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    // Quién la registró
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user")
    private UserEntity usuario;

    @Column(name = "motivo")
    private String motivo;

    // null si no hubo dinero a reintegrar (p. ej. cambio donde el cliente pagó diferencia)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "forma_reintegro", length = 20)
    private FormaReintegro formaReintegro;

    // Valor de lo devuelto (con el descuento de la venta original prorrateado)
    @Column(name = "monto_devuelto", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoDevuelto = BigDecimal.ZERO;

    // Total de la venta nueva (solo CAMBIO)
    @Column(name = "monto_nueva_venta", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoNuevaVenta = BigDecimal.ZERO;

    // montoNuevaVenta - montoDevuelto: > 0 el cliente paga, < 0 se le reintegra
    @Column(name = "diferencia", nullable = false, precision = 10, scale = 2)
    private BigDecimal diferencia = BigDecimal.ZERO;

    // Venta generada por el cambio
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_order_cambio")
    private OrderEntity ordenCambio;

    @OneToMany(mappedBy = "devolucion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DevolucionDetalleEntity> items = new ArrayList<>();
}
