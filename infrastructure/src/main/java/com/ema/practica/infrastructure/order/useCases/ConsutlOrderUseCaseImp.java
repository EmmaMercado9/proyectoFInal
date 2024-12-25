package com.ema.practica.infrastructure.order.useCases;

import com.ema.practica.core.order.model.Order;
import com.ema.practica.core.order.output.IRepositoryConsultOrder;
import com.ema.practica.infrastructure.order.crud.IConsultOrderCrud;
import com.ema.practica.infrastructure.order.entity.OrderEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class ConsutlOrderUseCaseImp implements IRepositoryConsultOrder {
    private final IConsultOrderCrud iConsultOrderCrud;
    @Autowired
    public ConsutlOrderUseCaseImp(IConsultOrderCrud iConsultOrderCrud){
        this.iConsultOrderCrud=iConsultOrderCrud;
    }

    @Override
    public Collection<Order> getOrders() {
        Iterable<OrderEntity> orderEntities = iConsultOrderCrud.findAll();
        return StreamSupport.stream(orderEntities.spliterator(), false).map(OrderEntity::entityToCore).collect(Collectors.toList());

    }

    public Order getOrderById(Long orderId){
        Optional<OrderEntity> orderEntity=(iConsultOrderCrud.findById(orderId));
        return orderEntity.map(OrderEntity::entityToCore).orElse(null);
    }

    public List<Order> getOrderByUser(String name){
       return this.iConsultOrderCrud.findByUser_Name(name).stream().map(OrderEntity::entityToCore).toList();
    }


}
