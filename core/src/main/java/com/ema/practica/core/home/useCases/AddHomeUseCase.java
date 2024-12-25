package com.ema.practica.core.home.useCases;

import com.ema.practica.core.home.input.IAddHomeInput;
import com.ema.practica.core.home.model.Home;
import com.ema.practica.core.home.output.IRepositoryAddHome;

public class AddHomeUseCase implements IAddHomeInput {

    IRepositoryAddHome iRepositoryAddHome;

    public AddHomeUseCase(IRepositoryAddHome iRepositoryAddHome) {
        this.iRepositoryAddHome = iRepositoryAddHome;
    }
    @Override
    public boolean addHome(Home home) {
            iRepositoryAddHome.save(home);
           return true;
    }
}
