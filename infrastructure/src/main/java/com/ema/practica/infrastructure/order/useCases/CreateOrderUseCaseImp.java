package com.ema.practica.infrastructure.order.useCases;


import com.ema.practica.infrastructure.order.crud.ICreateOrderCrud;
import com.ema.practica.infrastructure.order.entity.OrderEntity;
import com.ema.practica.infrastructure.order.rest.model.OrderRequest;
import com.ema.practica.infrastructure.util.user.crud.IListUserCrud;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Transactional
@Service
public class CreateOrderUseCaseImp {
    private final ICreateOrderCrud iCreateOrderCrud;

    private final IListUserCrud iListUserCrud;

    @Autowired
    public CreateOrderUseCaseImp(ICreateOrderCrud iCreateOrderCrud, IListUserCrud iListUserCrud){
        this.iCreateOrderCrud = iCreateOrderCrud;
        this.iListUserCrud = iListUserCrud;
    }
    public boolean saveOrder(OrderEntity order) {
        return iCreateOrderCrud.save(order).getIdOrder()!=null;
    }

    public void createOrder(OrderRequest orderRequest) {


        // Crear la orden
        OrderEntity orden = new OrderEntity();
        orden.setOrderDate(LocalDate.now());
        orden.setStatus("Pendiente");

    }
}