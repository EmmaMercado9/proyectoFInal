package com.ema.practica.infrastructure.order.useCases;

import com.ema.practica.core.order.model.Order;
import com.ema.practica.core.order.output.IRepositoryUpdateOrder;
import com.ema.practica.infrastructure.order.crud.IUpdateOrderStatusCrud;
import com.ema.practica.infrastructure.order.entity.OrderEntity;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service

public class UpdateOrderUseCaseImp implements IRepositoryUpdateOrder {

    IUpdateOrderStatusCrud iUpdateOrderStatusCrud;
    @Autowired
    public UpdateOrderUseCaseImp(IUpdateOrderStatusCrud iUpdateOrderStatus){
        this.iUpdateOrderStatusCrud=iUpdateOrderStatus;
    }
    @Override
    public Order current(Long id) {
        OrderEntity orderEntity=iUpdateOrderStatusCrud.findById(id).orElse(null);
        assert orderEntity != null;
        return (orderEntity.entityToCore());
    }

    @Override
    @Transactional
    public boolean update(Order order, Long id) {
        OrderEntity orderEntity=iUpdateOrderStatusCrud.findById(id).orElse(null);
        if(orderEntity!=null) {
            OrderEntity orderEntity1 = OrderEntity.coreToEntity(order);
            iUpdateOrderStatusCrud.save(orderEntity1);
            return true;}
        return false;
    }
}
