package com.ema.practica.infrastructure.user.rest.config;

import com.ema.practica.core.user.input.ICreateUserInput;
import com.ema.practica.core.user.output.IRepositoryCreateUser;
import com.ema.practica.core.user.usecases.CreateUserUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CreateUserConfig {

    @Bean
    public ICreateUserInput createUserInput(IRepositoryCreateUser repo){
        return new CreateUserUseCase(repo);

    }
}
