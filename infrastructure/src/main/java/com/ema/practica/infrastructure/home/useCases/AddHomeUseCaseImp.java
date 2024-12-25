package com.ema.practica.infrastructure.home.useCases;

import com.ema.practica.core.home.model.Home;
import com.ema.practica.core.home.output.IRepositoryAddHome;
import com.ema.practica.infrastructure.home.crud.IAddHomeCrud;
import com.ema.practica.infrastructure.home.entity.HomeEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AddHomeUseCaseImp implements IRepositoryAddHome {

    private  final IAddHomeCrud iAddHomeCrud;
    @Autowired
    public AddHomeUseCaseImp(IAddHomeCrud iAddHomeCrud){
        this.iAddHomeCrud=iAddHomeCrud;
    }


    @Override
    public boolean exists(Long id) {
        return iAddHomeCrud.existsById(id);
    }

    @Override
    public boolean save(Home home) {
        HomeEntity homeEntity= HomeEntity.coreToEntity(home);
        return iAddHomeCrud.save(homeEntity).getIdHome()!=null;
    }
}
