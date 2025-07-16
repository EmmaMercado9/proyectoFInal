package com.ema.practica.infrastructure.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.ema.practica.infrastructure.util.user.entitty.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Component
public class Jwt {
    private static final String SECRET_KEY = "K9B5jhdShb1aBjYD3g9F6KzLtXQ7Lm2S9Afhz59Yow4=";
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 ; // 1 hora de expiración

    // Generar una clave secreta segura
    public SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(UserEntity userEntity) {
        return createToken(userEntity);
    }

    public String createToken(UserEntity userEntity) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("idUser", userEntity.getIdUser());
        claims.put("email", userEntity.getMail());
        claims.put("name", userEntity.getName());

        return Jwts.builder()
                .claims(claims) // Asigna todos los claims juntos
                .subject(userEntity.getMail()) // Usamos el email como subject
                .issuedAt(new Date(System.currentTimeMillis())) // Fecha de emisión
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // Fecha de expiración
                .signWith(getSignInKey()) // Llave para firmar el token
                .compact();
    }
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject(); // El "subject" contiene el username o email
    }

        public boolean validateToken(String token,UserDetails userDetails) {

            String mail=extractUsername(token);
            return (mail.equals(userDetails.getUsername()) && !isTokenExpired(token));
        }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}

