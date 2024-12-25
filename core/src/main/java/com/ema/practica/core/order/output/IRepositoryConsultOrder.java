package com.ema.practica.core.order.output;

import com.ema.practica.core.order.model.Order;

import java.util.Collection;
import java.util.Collections;

public interface IRepositoryConsultOrder {

    Collection<Order> getOrders();
}
