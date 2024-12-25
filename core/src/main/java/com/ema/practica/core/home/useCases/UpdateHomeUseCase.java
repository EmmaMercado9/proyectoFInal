package com.ema.practica.core.home.useCases;

import com.ema.practica.core.home.input.IUpdateHomeInput;
import com.ema.practica.core.home.model.Home;
import com.ema.practica.core.home.output.IRepositoryUpdateHome;

public class UpdateHomeUseCase implements IUpdateHomeInput {

    IRepositoryUpdateHome iRepositoryUpdateHome;
    public UpdateHomeUseCase(IRepositoryUpdateHome iRepositoryUpdateHome){
        this.iRepositoryUpdateHome = iRepositoryUpdateHome;
    }


    @Override
    public boolean modifyHome(String street, String city, String Location, int nro, int postcode, Long id) {
        Home home =iRepositoryUpdateHome.get_Current_home(id);
        home.setStreet(street);
        home.setCity(city);
        home.setLocation(Location);
        home.setNro(nro);
        home.setPostcode(postcode);
        return iRepositoryUpdateHome.updateHome(home,id);
    }
}
