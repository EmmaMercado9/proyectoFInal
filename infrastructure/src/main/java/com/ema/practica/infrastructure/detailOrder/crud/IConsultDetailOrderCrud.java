package com.ema.practica.infrastructure.detailOrder.crud;

import com.ema.practica.core.detailOrder.model.DetailOrder;
import com.ema.practica.infrastructure.category.entity.CategoryProductEntity;
import com.ema.practica.infrastructure.detailOrder.entity.DetailOrderEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface IConsultDetailOrderCrud  extends CrudRepository<DetailOrderEntity,Long> {
}
