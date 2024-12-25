package com.ema.practica.core.order.output;

import com.ema.practica.core.order.model.Order;

public interface IRepositoryUpdateOrder {

    Order current(Long id);
    boolean update(Order order, Long id);
}
