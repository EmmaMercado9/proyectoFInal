package com.ema.practica.core.product.output;

import com.ema.practica.core.product.model.Product;

import java.util.Collection;

public interface IRepositoryListProduct {

    Collection<Product> getProducts();
}
