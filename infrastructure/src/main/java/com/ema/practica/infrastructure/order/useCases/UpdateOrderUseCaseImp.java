package com.ema.practica.infrastructure.order.useCases;
import com.ema.practica.infrastructure.order.crud.IUpdateOrderStatusCrud;
import com.ema.practica.infrastructure.order.entity.OrderEntity;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service

public class UpdateOrderUseCaseImp{

    IUpdateOrderStatusCrud iUpdateOrderStatusCrud;
    @Autowired
    public UpdateOrderUseCaseImp(IUpdateOrderStatusCrud iUpdateOrderStatus){
        this.iUpdateOrderStatusCrud=iUpdateOrderStatus;
    }
    public OrderEntity current(Long id) {
        OrderEntity orderEntity=iUpdateOrderStatusCrud.findById(id).orElse(null);
        assert orderEntity != null;
        return (orderEntity);
    }

    @Transactional
    public boolean update(OrderEntity orderEntity, Long id) {
        OrderEntity orderEntitys=iUpdateOrderStatusCrud.findById(id).orElse(null);
        if(orderEntitys!=null) {
            iUpdateOrderStatusCrud.save(orderEntity);
            return true;}
        return false;
    }
}
