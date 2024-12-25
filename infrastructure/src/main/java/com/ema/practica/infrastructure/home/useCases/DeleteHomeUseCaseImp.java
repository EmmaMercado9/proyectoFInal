package com.ema.practica.infrastructure.home.useCases;

import com.ema.practica.core.home.input.IDeleteHomeInput;
import com.ema.practica.core.home.model.Home;
import com.ema.practica.core.home.output.IRepositoryDeleteHome;
import com.ema.practica.infrastructure.home.crud.IDeleteHomeCrud;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeleteHomeUseCaseImp implements IRepositoryDeleteHome {

    private  final IDeleteHomeCrud iDeleteHomeCrud;
    @Autowired

    public  DeleteHomeUseCaseImp(IDeleteHomeCrud iDeleteHomeCrud) {
        this.iDeleteHomeCrud = iDeleteHomeCrud;
    }

    @Override
    public boolean deleteHome(Long id) {
        iDeleteHomeCrud.deleteById(id);
        return true;
    }
}
