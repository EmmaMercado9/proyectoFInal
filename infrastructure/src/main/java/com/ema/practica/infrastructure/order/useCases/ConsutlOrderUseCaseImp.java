package com.ema.practica.infrastructure.order.useCases;
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
public class ConsutlOrderUseCaseImp {
    private final IConsultOrderCrud iConsultOrderCrud;
    @Autowired
    public ConsutlOrderUseCaseImp(IConsultOrderCrud iConsultOrderCrud){
        this.iConsultOrderCrud=iConsultOrderCrud;
    }

    public Collection<OrderEntity> getOrders() {
        Iterable<OrderEntity> orderEntities = iConsultOrderCrud.findAll();
        return StreamSupport.stream(orderEntities.spliterator(), false).collect(Collectors.toList());

    }

    public OrderEntity getOrderById(Long orderId){
        Optional<OrderEntity> orderEntity=(iConsultOrderCrud.findById(orderId));
        return orderEntity.orElse(null);
    }

    public List<OrderEntity> getOrderByUser(String name){
       return this.iConsultOrderCrud.findByUser_Name(name).stream().toList();
    }


}
