package com.ema.practica.infrastructure.order.crud;

import com.ema.practica.infrastructure.order.entity.OrderEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ICreateOrderCrud  extends CrudRepository<OrderEntity,Long> {
}
