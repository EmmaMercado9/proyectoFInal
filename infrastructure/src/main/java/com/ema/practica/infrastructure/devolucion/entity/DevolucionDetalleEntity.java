package com.ema.practica.infrastructure.devolucion.entity;

import com.ema.practica.infrastructure.detailOrder.entity.OrderDetailEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

// Ítem devuelto: qué renglón de la venta, cuántas unidades y si volvió al stock
@Entity
@Table(name = "devolucion_detalles")
@Getter
@Setter
@NoArgsConstructor
public class DevolucionDetalleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_devolucion_detalle")
    private Long idDevolucionDetalle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_devolucion", nullable = false)
    private DevolucionEntity devolucion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_detail", nullable = false)
    private OrderDetailEntity orderDetail;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    // false = producto fallado: no vuelve al inventario
    @Column(name = "reingresa_stock", nullable = false)
    private Boolean reingresaStock = true;

    @Column(name = "monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    public DevolucionDetalleEntity(DevolucionEntity devolucion, OrderDetailEntity orderDetail,
                                   Integer quantity, Boolean reingresaStock, BigDecimal monto) {
        this.devolucion = devolucion;
        this.orderDetail = orderDetail;
        this.quantity = quantity;
        this.reingresaStock = reingresaStock;
        this.monto = monto;
    }
}
