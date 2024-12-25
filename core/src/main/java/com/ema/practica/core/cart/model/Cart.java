package com.ema.practica.core.cart.model;

import com.ema.practica.core.product.model.Product;
import com.ema.practica.core.user.model.User;


public class Cart {

    private Long idCart;
    private User user;
    private Product product;
    private int cantidad;

    public Cart() {}
    private Cart( Long idCart ,User user,Product product, int cantidad  ) {
        this.idCart = idCart;
        this.user=user;
        this.product=product;
        this.cantidad = cantidad;
    }

    public static Cart createCart( Long idCart ,User user,Product product, int cantidad  ) {
        return new Cart( idCart,user,product, cantidad );
    }



    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public Long getIdCart() {
        return idCart;
    }

    public void setIdCart(Long idCart) {
        this.idCart = idCart;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
