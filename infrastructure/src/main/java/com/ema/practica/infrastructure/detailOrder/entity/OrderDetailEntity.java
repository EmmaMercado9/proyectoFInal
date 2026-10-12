package com.ema.practica.infrastructure.detailOrder.entity;

import com.ema.practica.infrastructure.moduloProduct.product.entity.ProductEntity;
import com.ema.practica.infrastructure.moduloProduct.productVariant.entity.ProductVariantEntity;
import com.ema.practica.infrastructure.order.entity.OrderEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

// OrderDetailEntity.java
@Entity
@Table(name = "order_details")
@Getter
@Setter
@NoArgsConstructor
public class OrderDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detail")
    private Long idDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_order", nullable = false)
    private OrderEntity order;

    // CRÍTICO: apunta a la variante, no al producto
    // Así sabemos exactamente qué talla y color se vendió
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariantEntity variant;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    // Precio congelado al momento de la venta
    // Si mañana cambia el precio del producto, el histórico queda intacto
    @Column(name = "price_unit", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceUnit;

    // Subtotal precalculado: priceUnit × quantity
    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    // Unidades de este ítem que ya se devolvieron (devoluciones o cambios)
    @Column(name = "cantidad_devuelta", nullable = false, columnDefinition = "int default 0")
    private Integer cantidadDevuelta = 0;

    public int getCantidadDevueltaSegura() {
        return cantidadDevuelta != null ? cantidadDevuelta : 0;
    }

    /** Unidades que todavía se pueden devolver */
    public int getCantidadDevolvible() {
        return quantity - getCantidadDevueltaSegura();
    }

    // Constructor de negocio
    public OrderDetailEntity(OrderEntity order, ProductVariantEntity variant,
                             Integer quantity, BigDecimal priceUnit) {
        this.order     = order;
        this.variant   = variant;
        this.quantity  = quantity;
        this.priceUnit = priceUnit;
        this.subtotal  = priceUnit.multiply(BigDecimal.valueOf(quantity));
    }
}
