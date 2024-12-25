package com.ema.practica.infrastructure.order.rest.model;

import com.ema.practica.core.detailOrder.model.DetailOrder;
import com.ema.practica.core.home.model.Home;
import com.ema.practica.core.order.model.Order;
import com.ema.practica.core.user.model.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@Data

public class OrderDTO {


    @JsonProperty(value="idOrder")
    private Long idOrder;

    @JsonProperty(value="user")
    private User user;
    @JsonProperty(value="payment")
    private String payment;

    @JsonProperty(value="detail")
    private List<DetailOrder> orderList;

    @JsonProperty(value="orderDate")
    private LocalDate orderDate;

    @JsonProperty(value="totalAmount")
    private double totalAmount;

    @JsonProperty(value="status")
    private String status;

    public    Order dtoToCore() {
        return Order.getInstance(idOrder, user,payment,orderList,orderDate,totalAmount,status);
    }

    // Método para convertir de Core a DTO
    public  static OrderDTO coreToDTO(Order order) {
        return new OrderDTO(
                order.getIdOrder(),
                order.getUser(),
                order.getPayment(),
                order.getDetailOrders(),
                order.getOrderDate(),
                order.getTotalAmount(),
                order.getStatus()
        );
    }


}
