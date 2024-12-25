package com.ema.practica.infrastructure.home.rest.api;

import com.ema.practica.core.home.input.IUpdateHomeInput;
import com.ema.practica.core.home.model.Home;
import com.ema.practica.core.order.input.IUpdateOrderStatusInput;
import com.ema.practica.core.order.model.Order;
import com.ema.practica.infrastructure.home.rest.model.HomeDTO;
import com.ema.practica.infrastructure.order.rest.model.OrderDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/updateHome")
@CrossOrigin("*")

public class UpdateHomeController {

    IUpdateHomeInput iUpdateHomeInput;
    public UpdateHomeController(IUpdateHomeInput iUpdateHomeInput){
        this.iUpdateHomeInput=iUpdateHomeInput;
    }

    @PutMapping
    public ResponseEntity<?> modifyUser(@RequestBody HomeDTO homeDTO) {
        Home home=homeDTO.dtoToCore();
        boolean result = iUpdateHomeInput.modifyHome(
            home.getStreet(),
                home.getCity(),
                home.getLocation(),
                home.getNro(),
                home.getPostcode(),
                home.getIdHome()
        );

        return ResponseEntity.ok().body(result);
    }
}
