package com.ema.practica.infrastructure.order.rest.model;

import com.ema.practica.infrastructure.order.entity.OrderEntity;
import com.ema.practica.infrastructure.util.user.entitty.UserEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@Data

public class OrderDTO {


    @JsonProperty(value="idOrder")
    private Long idOrder;

    @JsonProperty(value="user")
    private UserEntity user;
    @JsonProperty(value="payment")
    private String payment;


    @JsonProperty(value="orderDate")
    private LocalDate orderDate;

    @JsonProperty(value="totalAmount")
    private double totalAmount;

    @JsonProperty(value="status")
    private String status;

    public OrderEntity dtoToEntity() {
        return new OrderEntity(idOrder, user,payment,orderDate,totalAmount,status);
    }

    // Método para convertir de Core a DTO
    public  static OrderDTO entityToDTO(OrderEntity orderEntity) {
        return new OrderDTO(
                orderEntity.getIdOrder(),
                orderEntity.getUser(),
                orderEntity.getPayment(),
                orderEntity.getOrderDate(),
                orderEntity.getTotalAmount(),
                orderEntity.getStatus()
        );
    }


}
