package com.ema.practica.infrastructure.user.rest.api;

import com.ema.practica.core.home.input.IAddHomeInput;
import com.ema.practica.core.home.model.Home;
import com.ema.practica.core.user.input.ICreateUserInput;
import com.ema.practica.infrastructure.home.useCases.ConsultHomeUseCaseImp;
import com.ema.practica.infrastructure.user.rest.model.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usersAdd")
@CrossOrigin("*")

public class CreateUserController {

    ICreateUserInput iCreateUserInput;
    private final ConsultHomeUseCaseImp consultHomeUseCaseImp;
    IAddHomeInput iAddHomeInput;
    @Autowired
    public CreateUserController(ICreateUserInput iCreateUserInput,IAddHomeInput iAddHomeInput, ConsultHomeUseCaseImp consultHomeUseCaseImp){
        this.iCreateUserInput=iCreateUserInput;
        this.consultHomeUseCaseImp = consultHomeUseCaseImp;
        this.iAddHomeInput=iAddHomeInput;
    }

    @PostMapping()
    public ResponseEntity<?> createUser(@RequestBody UserDTO user){
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        Home home=consultHomeUseCaseImp.createHome();
        user.setHome(home);
        boolean result= iCreateUserInput.createUser(user.dtoToCore());
        return  ResponseEntity.ok().body(result);
    }
}
