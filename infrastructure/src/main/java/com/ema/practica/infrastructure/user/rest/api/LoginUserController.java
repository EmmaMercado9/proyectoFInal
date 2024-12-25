package com.ema.practica.infrastructure.user.rest.api;

import com.ema.practica.core.user.input.ILoginUserInput;

import com.ema.practica.core.user.model.User;
import com.ema.practica.infrastructure.user.crud.ILoginUserCrud;
import com.ema.practica.infrastructure.user.entitty.UserEntity;
import com.ema.practica.infrastructure.user.rest.model.UserDTO;
import com.ema.practica.infrastructure.util.Jwt;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.AuthenticationException;

import java.util.Collections;


@RestController
@RequestMapping("/loggin")
@CrossOrigin("*")
public class LoginUserController {

    private final AuthenticationManager authenticationManager;
    private final Jwt jwtUtil; // Inyectar JwtUtil

    private final ILoginUserCrud iLoginUserCrud;
    @Autowired
    public LoginUserController(AuthenticationManager authenticationManager,Jwt jwtUtil,ILoginUserCrud iLoginUserCrud){
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.iLoginUserCrud = iLoginUserCrud;
    }

    @PostMapping
    public ResponseEntity<?> loginUser(@RequestBody UserDTO userDTO) {
            try {
                // Crear un token de autenticación basado en los datos del usuario
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(userDTO.getMail(), userDTO.getPassword())
                );

                System.out.println(userDTO.getIdUser()+userDTO.getRole());
                UserEntity userEntity =iLoginUserCrud.findFirstByMail(userDTO.getMail());
                // Si la autenticación es exitosa, generar el token
                String token = jwtUtil.generateToken(userEntity);
                return ResponseEntity.ok(Collections.singletonMap("token", token)); // Devolver el token
            } catch (AuthenticationException e) {
                // Si la autenticación falla, devolver una excepción de credenciales incorrectas
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");

            }
        }
}

