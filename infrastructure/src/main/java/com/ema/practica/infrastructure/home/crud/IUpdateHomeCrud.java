package com.ema.practica.infrastructure.home.crud;

import com.ema.practica.infrastructure.home.entity.HomeEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IUpdateHomeCrud  extends CrudRepository<HomeEntity,Long> {

}