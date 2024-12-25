package com.ema.practica.core.detailOrder.model;

import com.ema.practica.core.order.model.Order;
import com.ema.practica.core.product.model.Product;

public class DetailOrder {
    private Long idDetailOrder;

    private Product productList;
    private int amount;
    private  double priceUnitary;
    private double subtotalPrice;

    public DetailOrder(){}
    private DetailOrder (Long idDetailOrder,Product productList,int amount,double priceUnitary,double subtotalPrice) {
        this.idDetailOrder=idDetailOrder;
        this.productList=productList;
        this.amount=amount;
        this.priceUnitary=priceUnitary;
        this.subtotalPrice=subtotalPrice;
    }

    public static  DetailOrder getInstance(Long idDetailOrder, Product productList, int amount, double priceUnitary, double subtotalPrice) {
        return  new DetailOrder(idDetailOrder,productList,amount,priceUnitary,subtotalPrice);
    }

    public Long getIdDetailOrder() {
        return idDetailOrder;
    }

    public void setIdDetailOrder(Long idDetailOrder) {
        this.idDetailOrder = idDetailOrder;
    }

    public int getAmount() {
        return amount;
    }

    public Product getProductList() {
        return productList;
    }

    public void setProductList(Product productList) {
        this.productList = productList;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public double getPriceUnitary() {
        return priceUnitary;
    }

    public void setPriceUnitary(double priceUnitary) {
        this.priceUnitary = priceUnitary;
    }

    public double getSubtotalPrice() {
        return subtotalPrice;
    }

    public void setSubtotalPrice(double subtotalPrice) {
        this.subtotalPrice = subtotalPrice;
    }

}
