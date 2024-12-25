package com.ema.practica.core.order.useCases;

import com.ema.practica.core.order.input.IConsultOrderInput;
import com.ema.practica.core.order.input.ICreateOrderInput;
import com.ema.practica.core.order.model.Order;
import com.ema.practica.core.order.output.IRepositoryConsultOrder;
import com.ema.practica.core.order.output.IRepositoryCreateOrder;

import java.util.Collection;

public class ConsutlOrderUseCase implements IConsultOrderInput {

    IRepositoryConsultOrder IRepositoryConsultOrder;
    public ConsutlOrderUseCase(IRepositoryConsultOrder IRepositoryConsultOrder) {
        this.IRepositoryConsultOrder = IRepositoryConsultOrder;
    }

    @Override
    public Collection<Order> getOrders() {
        return IRepositoryConsultOrder.getOrders();
    }
}
