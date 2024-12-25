package com.ema.practica.core.product.output;

import com.ema.practica.core.product.model.Product;
import com.ema.practica.core.user.model.User;

public interface IRepositoryModifiProduct {

    boolean exists(Long id);
    Product get_Current_product(Long idCurrenProduc);
    boolean modifyProduct(Product product,Long idCurrenProduc);
}
