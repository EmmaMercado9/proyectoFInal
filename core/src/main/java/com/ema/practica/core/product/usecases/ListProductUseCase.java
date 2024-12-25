package com.ema.practica.core.product.usecases;

import com.ema.practica.core.product.input.IListProductInput;
import com.ema.practica.core.product.model.Product;
import com.ema.practica.core.product.output.IRepositoryListProduct;

import java.util.Collection;

public class ListProductUseCase implements IListProductInput {

    IRepositoryListProduct iRepositoryListProduct;

    public ListProductUseCase(IRepositoryListProduct iRepositoryListProduct) {
        this.iRepositoryListProduct = iRepositoryListProduct;
    }
    @Override
    public Collection<Product> getProducts() {
      return iRepositoryListProduct.getProducts();

    }
}
