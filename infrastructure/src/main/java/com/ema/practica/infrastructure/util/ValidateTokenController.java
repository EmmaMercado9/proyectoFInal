package com.ema.practica.infrastructure.util;

import com.ema.practica.infrastructure.util.Jwt;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class ValidateTokenController {
        private final Jwt jwt;

        public ValidateTokenController(Jwt jwt) {
            this.jwt = jwt;
        }

        @PostMapping("/validate")
        public ResponseEntity<?> validateToken(@RequestBody String token) {
            try {
                Jwts.parser()
                        .verifyWith(jwt.getSignInKey())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
                return ResponseEntity.ok(true);
            } catch (JwtException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
            }
        }
}

