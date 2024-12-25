package com.ema.practica.infrastructure.home.crud;

import com.ema.practica.core.home.model.Home;
import com.ema.practica.infrastructure.detailOrder.entity.DetailOrderEntity;
import com.ema.practica.infrastructure.home.entity.HomeEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IAddHomeCrud  extends CrudRepository<HomeEntity,Long> {

}
