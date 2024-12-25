package com.ema.practica.infrastructure.user.entitty;

import com.ema.practica.core.home.model.Home;
import com.ema.practica.infrastructure.home.entity.HomeEntity;
import com.ema.practica.infrastructure.user.entitty.UserEntity;
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
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_"+userEntity.getRole()));
    }

    @Override
    public String getPassword() {
        return userEntity.getPassword();
    }

    @Override
    public String getUsername() {
        return userEntity.getMail();
    }

    public Long getId(){
        return userEntity.getIdUser();
    }
    public String getname(){
        return userEntity.getName();
    }

    public HomeEntity getHome(){
        return userEntity.getHome();
    }
}
