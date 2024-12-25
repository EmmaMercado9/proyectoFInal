package com.ema.practica.infrastructure.home.useCases;

import com.ema.practica.core.home.model.Home;
import com.ema.practica.core.home.output.IRepositoryConsultHome;
import com.ema.practica.infrastructure.home.crud.IConsultHomeCrud;
import com.ema.practica.infrastructure.home.entity.HomeEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class ConsultHomeUseCaseImp implements IRepositoryConsultHome {

    private final IConsultHomeCrud iConsultHomeCrud;
    @Autowired
    public ConsultHomeUseCaseImp(IConsultHomeCrud iConsultHomeCrud) {
        this.iConsultHomeCrud = iConsultHomeCrud;
    }

    @Override
    public Collection<Home> getHomeList() {
        Iterable<HomeEntity> homeEntities = iConsultHomeCrud.findAll();
        return StreamSupport.stream(homeEntities.spliterator(), false).map(HomeEntity::entityToCore).collect(Collectors.toList());

    }

    public Home createHome() {
        HomeEntity homeEntity=new HomeEntity();
        return iConsultHomeCrud.save(homeEntity).entityToCore();
    }
}
