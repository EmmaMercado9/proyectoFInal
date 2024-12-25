package com.ema.practica.core.order.input;

import com.ema.practica.core.user.model.User;

import java.time.LocalDate;

public interface IUpdateOrderStatusInput {

    boolean updateOrder( User user, String payment, LocalDate orderDate, double totalAmount,String stauts,Long id);
}
