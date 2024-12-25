package com.ema.practica.infrastructure.detailOrder.crud;

import com.ema.practica.core.product.model.Product;
import com.ema.practica.infrastructure.detailOrder.entity.DetailOrderEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IUpdateDetailOrderCrud extends CrudRepository<DetailOrderEntity,Long> {

}
