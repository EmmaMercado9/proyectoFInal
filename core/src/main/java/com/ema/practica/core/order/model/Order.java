package com.ema.practica.core.order.model;

import com.ema.practica.core.detailOrder.model.DetailOrder;

import com.ema.practica.core.user.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private Long idOrder;
    private User user;
    private String payment;
    private List<DetailOrder> detailOrders = new ArrayList<>();

    private LocalDate orderDate;
    private double totalAmount;

    private String status;

    public Order(){}
    private Order(Long idOrder, User user, String payment,List<DetailOrder> detailOrderArrayList, LocalDate orderDate, double totalAmount,String status) {
        this.idOrder = idOrder;
        this.user = user;
        this.payment = payment;
        this.detailOrders=detailOrderArrayList;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.status=status;
    }

    public static Order getInstance(Long idOrder, User user, String payment,List<DetailOrder> detailOrders, LocalDate orderDate, double totalAmount,String status) {
        return new Order(idOrder, user, payment,detailOrders, orderDate, totalAmount,status);
    }

    public Long getIdOrder() {
        return idOrder;
    }

    public void setIdOrder(Long idOrder) {
        this.idOrder = idOrder;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPayment() {
        return payment;
    }

    public void setPayment(String payment) {
        this.payment = payment;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<DetailOrder> getDetailOrders() {
        return detailOrders;
    }

    public void setDetailOrders(List<DetailOrder> detailOrders) {
        this.detailOrders = detailOrders;
    }
}
