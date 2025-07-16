package com.ema.practica.infrastructure.util.user.entitty;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@AllArgsConstructor
@NoArgsConstructor

public class SecurityUser implements UserDetails {

    private UserEntity userEntity;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_"+userEntity.getRol().name()));
    }

    @Override
    public String getPassword() {
        return userEntity.getPassword();
    }

    @Override
    public String getUsername() {
        return userEntity.getMail();
    }

    public UserEntity getUserEntity() {
        return userEntity;
    }
}
