package com.ema.practica.core.home.output;

import com.ema.practica.core.home.model.Home;

public interface IRepositoryAddHome {


    boolean exists(Long id);
    boolean save(Home home);
}
