package com.ema.practica.infrastructure.util.user.rest.api;

import com.ema.practica.infrastructure.util.user.rest.model.UserDTO;
import com.ema.practica.infrastructure.util.user.useCases.CreateUserUseCaseImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/usersAdd")

public class CreateUserController {

    private final PasswordEncoder passwordEncoder;
    private final CreateUserUseCaseImp createUserUseCaseImp;
    @Autowired
    public CreateUserController(CreateUserUseCaseImp createUserUseCaseImp,PasswordEncoder passwordEncoder){
    this.createUserUseCaseImp=createUserUseCaseImp;
    this.passwordEncoder=passwordEncoder;
    }

    @PostMapping()
    public ResponseEntity<?> createUser(@RequestBody UserDTO user) {

        String username=SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("El username es  " +username);
        if (createUserUseCaseImp.UserExist(user.getMail())) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "El mail ya existe");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        // Mejor inyectar este encoder, no crear uno nuevo aquí
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        boolean result = createUserUseCaseImp.SaveUser(user,username);

        if (result) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Usuario creado correctamente");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "No se pudo crear el usuario");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
