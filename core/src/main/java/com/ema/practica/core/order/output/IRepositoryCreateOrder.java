package com.ema.practica.core.order.output;

import com.ema.practica.core.order.model.Order;

public interface IRepositoryCreateOrder {

    boolean saveOrder(Order order);
}
