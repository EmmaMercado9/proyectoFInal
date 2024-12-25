package com.ema.practica.infrastructure.home.crud;

import com.ema.practica.core.home.model.Home;
import com.ema.practica.infrastructure.home.entity.HomeEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface IConsultHomeCrud  extends CrudRepository<HomeEntity,Long> {

    HomeEntity findByIdHome(Long id);

}
