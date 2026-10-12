package com.ema.practica.infrastructure.util;

import com.ema.practica.infrastructure.util.user.entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Component
public class Jwt {
    private final SecretKey signInKey;
    private final long expirationMs;

    // El secreto viene de la variable de entorno JWT_SECRET (Base64, mínimo 32 bytes)
    public Jwt(@Value("${jwt.secret}") String secret,
               @Value("${jwt.expiration-ms:3600000}") long expirationMs) {
        this.signInKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationMs = expirationMs;
    }

    public SecretKey getSignInKey() {
        return signInKey;
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
                .expiration(new Date(System.currentTimeMillis() + expirationMs)) // Fecha de expiración
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

