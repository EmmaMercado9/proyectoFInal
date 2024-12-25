package com.ema.practica.infrastructure.cart.entity;

import com.ema.practica.core.cart.model.Cart;
import com.ema.practica.core.category.model.CategoryProduct;
import com.ema.practica.core.product.model.Product;
import com.ema.practica.core.user.model.User;
import com.ema.practica.infrastructure.category.entity.CategoryProductEntity;
import com.ema.practica.infrastructure.product.entity.ProductEntity;
import com.ema.practica.infrastructure.user.entitty.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "carts")

public class CartEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "idcart")
    private Long idCart;
    @ManyToOne()
    private UserEntity user;
    @ManyToOne()
    private ProductEntity product;
    @Column(name = "cantidad")
    private int cantidad;

    public CartEntity(UserEntity userEntity, ProductEntity productEntity, int cantidad) {
        this.user = userEntity;
        this.product = productEntity;
        this.cantidad = cantidad;
    }
    public Cart entityToCore() {
        return Cart.createCart(this.getIdCart(),this.getUser().entityToCore(),this.getProduct().entityToCore(),this.getCantidad());
    }

    public static CartEntity coreToEntity(Cart cart) {
        return new CartEntity(cart.getIdCart(),UserEntity.coreToEntity(cart.getUser()),ProductEntity.coreToEntity(cart.getProduct()),cart.getCantidad());
    }
}
