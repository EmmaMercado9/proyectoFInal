package com.ema.practica.core.product.usecases;

import com.ema.practica.core.product.input.IDeleteProductInput;
import com.ema.practica.core.product.model.Product;
import com.ema.practica.core.product.output.IRepositoryDeleteProduct;

public class DeleteProductUseCases implements IDeleteProductInput {

    IRepositoryDeleteProduct iRepositoryDeleteProduct;
    public DeleteProductUseCases(IRepositoryDeleteProduct iRepositoryDeleteProduct){
        this.iRepositoryDeleteProduct = iRepositoryDeleteProduct;
    }
    @Override
    public boolean deleteProduct(Long product_id) {
        if(iRepositoryDeleteProduct.exists(product_id)){
            return iRepositoryDeleteProduct.deleteProduct(product_id);

        }
        return false;
    }
}
