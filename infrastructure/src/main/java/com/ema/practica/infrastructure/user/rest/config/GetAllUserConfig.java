package com.ema.practica.infrastructure.user.rest.config;

import com.ema.practica.core.user.input.IListUserInput;
import com.ema.practica.core.user.output.IRepositoryListUser;
import com.ema.practica.core.user.usecases.ListUserUseCase;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GetAllUserConfig {

    @Bean
    public IListUserInput IListUser (IRepositoryListUser repo){
        return new ListUserUseCase(repo);
    }
}
