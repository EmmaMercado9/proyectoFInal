package com.ema.practica.infrastructure.util.user.rest.api;

import com.ema.practica.infrastructure.util.user.crud.ILoginUserCrud;
import com.ema.practica.infrastructure.util.user.entity.SecurityUser;
import com.ema.practica.infrastructure.util.user.entity.UserEntity;
import com.ema.practica.infrastructure.util.user.rest.model.RequestLogin;
import com.ema.practica.infrastructure.util.user.rest.model.UserDTO;
import com.ema.practica.infrastructure.util.Jwt;
import com.ema.practica.infrastructure.util.user.useCases.LoginUserUseCaseImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.AuthenticationException;
import java.util.Collections;
@RestController
// "/loggin" se mantiene como alias para clientes viejos; usar "/login"
@RequestMapping({"/login", "/loggin"})
public class LoginUserController {
    private final AuthenticationManager authenticationManager;
    private final Jwt jwtUtil;
    private final LoginUserUseCaseImp  loginUserUseCaseImp;
    @Autowired
    public LoginUserController(AuthenticationManager authenticationManager,Jwt jwtUtil,LoginUserUseCaseImp loginUserUseCaseImp){
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.loginUserUseCaseImp = loginUserUseCaseImp;
    }
    @PostMapping
    public ResponseEntity<?> loginUser(@RequestBody RequestLogin requestLogin) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requestLogin.getEmail(), requestLogin.getPassword())
            );
            SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
            if (!loginUserUseCaseImp.loginValidateStatus(securityUser.getUserEntity().getStatus())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid user se encuentra inactivo");
            }
            String token = jwtUtil.generateToken(securityUser.getUserEntity());
            return ResponseEntity.ok(Collections.singletonMap("token", token));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }
}