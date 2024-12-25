package com.ema.practica.infrastructure.home.rest.api;

import com.ema.practica.core.cart.input.IConsultCartInput;
import com.ema.practica.core.cart.model.Cart;
import com.ema.practica.core.home.input.IConsultHomeInput;
import com.ema.practica.core.home.model.Home;
import com.ema.practica.infrastructure.home.rest.model.HomeDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/getAllHome")
@CrossOrigin("*")

public class GetAllHomeController {
    IConsultHomeInput iConsultHomeInput;

    public GetAllHomeController(IConsultHomeInput iConsultHomeInput){
        this.iConsultHomeInput=iConsultHomeInput;
    }

    @GetMapping()

    public ResponseEntity<List<HomeDTO>> GetAllUsers(){
        Collection<Home> list=iConsultHomeInput.getHome();

        List<HomeDTO> list1=list.stream().map(HomeDTO::coreToDTO).toList();

        if (!list1.isEmpty()) {
            return ResponseEntity.ok(list1);
        } else {
            return ResponseEntity.noContent().build();
        }
    }


}
