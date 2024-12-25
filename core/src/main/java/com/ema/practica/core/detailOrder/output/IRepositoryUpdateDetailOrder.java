package com.ema.practica.core.detailOrder.output;

import com.ema.practica.core.detailOrder.model.DetailOrder;
import com.ema.practica.core.product.model.Product;

import java.util.List;

public interface IRepositoryUpdateDetailOrder {

    DetailOrder currentDetailOrder(Long id);
    boolean updateDetailOrder(DetailOrder detailOrder, Long id);
}
