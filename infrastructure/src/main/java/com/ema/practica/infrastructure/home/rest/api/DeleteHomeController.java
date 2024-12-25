package com.ema.practica.infrastructure.home.rest.api;

import com.ema.practica.core.cart.input.IDeleteProductCartInput;
import com.ema.practica.core.cart.model.Cart;
import com.ema.practica.core.home.input.IDeleteHomeInput;
import com.ema.practica.core.home.model.Home;
import com.ema.practica.infrastructure.home.rest.model.HomeDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/deleteHome")
@CrossOrigin("*")

public class DeleteHomeController {

    IDeleteHomeInput IDeleteHomeInput;
    public DeleteHomeController(IDeleteHomeInput IDeleteHomeInput){
        this.IDeleteHomeInput=IDeleteHomeInput;
    }

    @DeleteMapping()
    public ResponseEntity<?> deleteHome(@RequestBody HomeDTO homeDTO){
        Home home=homeDTO.dtoToCore();
        boolean resultado=IDeleteHomeInput.deleteHome(home);
        return ResponseEntity.ok().body(resultado);
    }
}
