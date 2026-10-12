package com.ema.practica.infrastructure.util;

import com.ema.practica.infrastructure.util.Jwt;
import com.ema.practica.infrastructure.util.model.TokenDto;
import com.ema.practica.infrastructure.util.servicesDetailUser.UserDetailServiceImp;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class ValidateTokenController {
        private final Jwt jwt;
    private final UserDetailServiceImp userDetailServiceImp;



    @Autowired
    public ValidateTokenController(UserDetailServiceImp userDetailServiceImp,Jwt jwt) {

            this.userDetailServiceImp = userDetailServiceImp;
            this.jwt = jwt;
        }

        @PostMapping("/validate")
        public ResponseEntity<?> validateToken(@RequestBody TokenDto token) {

            try {
                String username=jwt.extractUsername(token.getToken());
                UserDetails userDetails= userDetailServiceImp.loadUserByUsername(username);
                boolean result=jwt.validateToken(token.getToken(),userDetails);
                if (result) {
                    return ResponseEntity.ok(true);
                } else {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("no es valido el token");
                }
            } catch (JwtException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
            }
        }
}

