package com.ema.practica.infrastructure.home.useCases;

import com.ema.practica.core.home.model.Home;
import com.ema.practica.core.home.output.IRepositoryUpdateHome;
import com.ema.practica.infrastructure.home.crud.IUpdateHomeCrud;
import com.ema.practica.infrastructure.home.entity.HomeEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UpdateHomeUseCaseImp implements IRepositoryUpdateHome {

    private  final IUpdateHomeCrud iUpdateHomeCrud;
    @Autowired
    public UpdateHomeUseCaseImp(IUpdateHomeCrud iUpdateHomeCrud) {
        this.iUpdateHomeCrud = iUpdateHomeCrud;
    }



    @Override
    public boolean updateHome(Home home, Long id) {
        HomeEntity homeEntity=iUpdateHomeCrud.findById(id).orElse(null);
        if(homeEntity!=null) {
            HomeEntity homeEntity1 =HomeEntity.coreToEntity(home);
            iUpdateHomeCrud.save(homeEntity1);
            return true;
        }
        return false;
    }

    @Override
    public Home get_Current_home(Long idCurrenhome) {
        HomeEntity homeEntity=iUpdateHomeCrud.findById(idCurrenhome).orElse(null);
        assert homeEntity != null;
        return (homeEntity.entityToCore());
    }
}
