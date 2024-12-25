package com.ema.practica.infrastructure.detailOrder.crud;

import com.ema.practica.core.detailOrder.model.DetailOrder;
import com.ema.practica.infrastructure.detailOrder.entity.DetailOrderEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ICreateDetailOrderCrud extends CrudRepository<DetailOrderEntity,Long> {

}
