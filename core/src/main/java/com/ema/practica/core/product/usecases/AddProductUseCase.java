package com.ema.practica.core.product.usecases;

import com.ema.practica.core.product.input.IAddProductInput;
import com.ema.practica.core.product.model.Product;
import com.ema.practica.core.product.output.IRepositoryAddProuct;

public class AddProductUseCase implements IAddProductInput {
    IRepositoryAddProuct iRepositoryAddProuct;

    public AddProductUseCase(IRepositoryAddProuct iRepositoryAddProuct) {
        this.iRepositoryAddProuct = iRepositoryAddProuct;
    }
    @Override
    public boolean addProduct(Product product) {
     return iRepositoryAddProuct.SaveUser(product);
    }
}
