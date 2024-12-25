package com.ema.practica.core.order.input;

import com.ema.practica.core.order.model.Order;

import java.util.Collection;

public interface IConsultOrderInput {
    Collection<Order> getOrders();
}
