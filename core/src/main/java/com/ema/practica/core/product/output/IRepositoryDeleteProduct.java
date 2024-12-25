package com.ema.practica.core.product.output;

public interface IRepositoryDeleteProduct {

    boolean exists(Long id);
    boolean deleteProduct(Long productId);
}
