package com.ema.practica.core.order.useCases;

import com.ema.practica.core.order.input.ICreateOrderInput;
import com.ema.practica.core.order.model.Order;
import com.ema.practica.core.order.output.IRepositoryCreateOrder;

public class CreateOrderUseCase implements ICreateOrderInput {

    IRepositoryCreateOrder iRepositoryCreateOrder;
    public CreateOrderUseCase(IRepositoryCreateOrder iRepositoryCreateOrder) {
        this.iRepositoryCreateOrder = iRepositoryCreateOrder;
    }
    @Override
    public boolean addOrder(Order order) {
        return iRepositoryCreateOrder.saveOrder(order);
    }
}
