package com.ema.practica.infrastructure.user.rest.config;

import com.ema.practica.core.user.input.ILoginUserInput;
import com.ema.practica.core.user.output.IRepositoryLogin;
import com.ema.practica.core.user.usecases.LoginUserUseCase;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoginUserConfig {
    @Bean
    public ILoginUserInput iLoginInput(IRepositoryLogin iRepositoryLogin){
        return  new LoginUserUseCase(iRepositoryLogin);
    }

}
