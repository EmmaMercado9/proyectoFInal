package com.ema.practica.infrastructure.user.rest.api;

import com.ema.practica.core.product.input.IDeleteProductInput;
import com.ema.practica.core.product.model.Product;
import com.ema.practica.core.user.input.IDeleteUserInput;
import com.ema.practica.core.user.model.User;
import com.ema.practica.infrastructure.product.rest.model.ProductDTO;
import com.ema.practica.infrastructure.user.rest.model.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/deleteUser")
@CrossOrigin("*")

public class DeleteUserController {

    IDeleteUserInput iDeleteUserInput;
    @Autowired
    public DeleteUserController(IDeleteUserInput iDeleteUserInput){
        this.iDeleteUserInput=iDeleteUserInput;
    }

    @DeleteMapping()
    public ResponseEntity<?> deleteUser(@RequestBody UserDTO userDTO){
        User user=userDTO.dtoToCore();
        boolean resultado=iDeleteUserInput.DeleteUserInput(user);
        return ResponseEntity.ok().body(resultado);
    }
}
