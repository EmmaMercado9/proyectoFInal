package com.ema.practica.core.detailOrder.input;

import com.ema.practica.core.detailOrder.model.DetailOrder;
import com.ema.practica.core.product.model.Product;

import java.util.List;

public interface IUpdateDetailOrderInput {

    boolean updateDetailOrder(Product productList, int amount, double priceUnitary, double subtotalPrice, Long id);
}
