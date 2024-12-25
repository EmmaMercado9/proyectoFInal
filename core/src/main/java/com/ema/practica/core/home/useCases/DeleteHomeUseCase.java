package com.ema.practica.core.home.useCases;

import com.ema.practica.core.home.input.IDeleteHomeInput;
import com.ema.practica.core.home.model.Home;
import com.ema.practica.core.home.output.IRepositoryDeleteHome;

public class DeleteHomeUseCase  implements IDeleteHomeInput {

    IRepositoryDeleteHome iRepositoryDeleteHome;
    public DeleteHomeUseCase(IRepositoryDeleteHome iRepositoryDeleteHome){
        this.iRepositoryDeleteHome = iRepositoryDeleteHome;
    }

    @Override
    public boolean deleteHome(Home home) {
        return iRepositoryDeleteHome.deleteHome(home.getIdHome());
    }
}
