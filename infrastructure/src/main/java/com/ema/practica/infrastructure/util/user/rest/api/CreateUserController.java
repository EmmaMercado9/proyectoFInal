package com.ema.practica.infrastructure.user.rest.api;

import com.ema.practica.infrastructure.user.rest.model.UserDTO;
import com.ema.practica.infrastructure.user.useCases.CreateUserUseCaseImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usersAdd")
@CrossOrigin("*")

public class CreateUserController {

    private final CreateUserUseCaseImp createUserUseCaseImp;
    @Autowired
    public CreateUserController(CreateUserUseCaseImp createUserUseCaseImp){
    this.createUserUseCaseImp=createUserUseCaseImp;
    }

    @PostMapping()
    public ResponseEntity<?> createUser(@RequestBody UserDTO user){
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));

        boolean result= createUserUseCaseImp.SaveUser(user.dtoToEntity());
        return  ResponseEntity.ok().body(result);
    }
}
