package com.ema.practica.infrastructure.order.entity;
import com.ema.practica.core.order.model.Order;
import com.ema.practica.infrastructure.detailOrder.entity.DetailOrderEntity;
import com.ema.practica.infrastructure.user.entitty.UserEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.*;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "orders")

public class OrderEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "idorder")
    private Long idOrder;
    @ManyToOne(optional = false,cascade = CascadeType.ALL ,fetch = FetchType.EAGER)
    @JsonIgnore
    @JoinColumn (name = "iduser",referencedColumnName = "iduser")
    private UserEntity user;
    @Column(name="payment")
    private String payment;
    @OneToMany(cascade = CascadeType.ALL ,fetch = FetchType.EAGER)
    @JsonIgnore
    private List<DetailOrderEntity> detailOrders = new ArrayList<>();
    @Column(name = "orderdate")
    private LocalDate orderDate;
    @Column(name = "totalamount")
    private double totalAmount;
    @Column(name = "status")
    private String status;

    public OrderEntity(double v, LocalDate now, UserEntity userEntity,String status) {
        this.totalAmount=v;
        this.orderDate = now;
        this.user = userEntity;
        this.status = status;
    }

    public Order entityToCore() {
        return Order.getInstance(this.getIdOrder(), this.getUser().entityToCore(),this.getPayment(), this.getDetailOrders().stream().map(DetailOrderEntity::entityToCore).toList(),this.getOrderDate(),this.getTotalAmount(),this.getStatus());
    }

    public static OrderEntity coreToEntity(Order order) {

        return new OrderEntity(order.getIdOrder(),UserEntity.coreToEntity(order.getUser()),order.getPayment(),order.getDetailOrders().stream().map(DetailOrderEntity::coreToEntity).toList(),order.getOrderDate(),order.getTotalAmount(),order.getStatus());
    }
}
