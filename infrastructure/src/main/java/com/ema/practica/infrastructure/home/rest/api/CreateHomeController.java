package com.ema.practica.infrastructure.home.rest.api;

import com.ema.practica.core.cart.input.IAddProductCartInput;
import com.ema.practica.core.home.input.IAddHomeInput;
import com.ema.practica.infrastructure.home.rest.model.HomeDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/AddHome")
@CrossOrigin("*")

public class CreateHomeController {

    IAddHomeInput iAddHomeInput;
    public CreateHomeController(IAddHomeInput iAddHomeInput ){
        this.iAddHomeInput=iAddHomeInput;
    }

    @PostMapping()
    public ResponseEntity<?> addProductCart(@RequestBody HomeDTO homeDTO){
        boolean result= iAddHomeInput.addHome(homeDTO.dtoToCore());
        return  ResponseEntity.ok().body(result);
    }
}
