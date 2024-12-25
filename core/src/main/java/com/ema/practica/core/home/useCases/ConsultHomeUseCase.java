package com.ema.practica.core.home.useCases;

import com.ema.practica.core.home.input.IConsultHomeInput;
import com.ema.practica.core.home.model.Home;
import com.ema.practica.core.home.output.IRepositoryConsultHome;

import java.util.Collection;

public class ConsultHomeUseCase implements IConsultHomeInput {
    IRepositoryConsultHome iRepositoryConsultHome;

    public ConsultHomeUseCase(IRepositoryConsultHome iRepositoryConsultHome) {
        this.iRepositoryConsultHome = iRepositoryConsultHome;
    }

    @Override
    public Collection<Home> getHome() {
        return  iRepositoryConsultHome.getHomeList();
    }
}
