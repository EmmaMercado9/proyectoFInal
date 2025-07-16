package com.ema.practica.infrastructure.order.entity;
import com.ema.practica.infrastructure.util.user.entitty.UserEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

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
}
