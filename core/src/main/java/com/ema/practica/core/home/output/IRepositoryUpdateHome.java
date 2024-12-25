package com.ema.practica.core.home.output;

import com.ema.practica.core.home.model.Home;
import com.ema.practica.core.product.model.Product;

public interface IRepositoryUpdateHome {

    boolean updateHome(Home home, Long id);
    Home get_Current_home(Long idCurrenhome);

}
