package com.ema.practica.infrastructure.detailOrder.entity;

import com.ema.practica.core.detailOrder.model.DetailOrder;
import com.ema.practica.infrastructure.product.entity.ProductEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "detailorders")
public class DetailOrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddetail")
    private Long idDetailOrder;
    @ManyToOne(optional = false,cascade = CascadeType.DETACH ,fetch = FetchType.EAGER)
    @JoinColumn(name="idproduct",referencedColumnName = "idproduct")
    private ProductEntity product;
    @Column(name = "amount")
    private int amount;
    @Column(name = "priceunitary")
    private  double priceUnitary;
    @Column(name = "subtotal")
    private double subtotalPrice;

    public DetailOrder entityToCore() {

    return DetailOrder.getInstance(this.getIdDetailOrder(),this.getProduct().entityToCore(),this.getAmount(),this.getPriceUnitary(),this.getSubtotalPrice());

    }

    public static DetailOrderEntity coreToEntity(DetailOrder detailOrder) {

        return new DetailOrderEntity(detailOrder.getIdDetailOrder(),ProductEntity.coreToEntity(detailOrder.getProductList()),detailOrder.getAmount(),detailOrder.getPriceUnitary(),detailOrder.getSubtotalPrice());
    }
}
