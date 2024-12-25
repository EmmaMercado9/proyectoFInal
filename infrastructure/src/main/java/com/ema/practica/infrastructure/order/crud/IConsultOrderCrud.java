package com.ema.practica.infrastructure.order.crud;

import com.ema.practica.infrastructure.home.entity.HomeEntity;
import com.ema.practica.infrastructure.order.entity.OrderEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface IConsultOrderCrud  extends CrudRepository<OrderEntity,Long> {

    List<OrderEntity> findByUser_Name(String name);
}
