package com.ema.practica.core.order.useCases;

import com.ema.practica.core.order.input.IUpdateOrderStatusInput;
import com.ema.practica.core.order.model.Order;
import com.ema.practica.core.order.output.IRepositoryUpdateOrder;
import com.ema.practica.core.user.model.User;

import java.time.LocalDate;

public class UpdateOrderUseCase implements IUpdateOrderStatusInput {
    IRepositoryUpdateOrder iRepositoryUpdateOrder;
    public UpdateOrderUseCase(IRepositoryUpdateOrder iRepositoryUpdateOrder){
        this.iRepositoryUpdateOrder = iRepositoryUpdateOrder;
    }




    @Override
    public boolean updateOrder(User user, String payment, LocalDate orderDate, double totalAmount,String status, Long id) {
        Order order  =iRepositoryUpdateOrder.current(id);
        order.setUser(user);
        order.setPayment(payment);
        order.setOrderDate(orderDate);
        order.setTotalAmount(totalAmount);
        order.setStatus(status);
        return iRepositoryUpdateOrder.update(order,id);

    }
}
