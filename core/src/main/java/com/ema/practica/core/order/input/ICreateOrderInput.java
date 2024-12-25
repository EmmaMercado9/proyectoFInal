package com.ema.practica.core.order.input;

import com.ema.practica.core.order.model.Order;

public interface ICreateOrderInput {
    boolean addOrder(Order order);

}
