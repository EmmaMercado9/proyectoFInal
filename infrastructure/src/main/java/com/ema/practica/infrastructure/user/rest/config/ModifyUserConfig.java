package com.ema.practica.infrastructure.user.rest.config;


import com.ema.practica.core.user.input.IModifyUserInput;
import com.ema.practica.core.user.output.IRepositoryModifyUser;
import com.ema.practica.core.user.usecases.ModifyUserUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModifyUserConfig {
    @Bean
    public IModifyUserInput iModifyUserInput(IRepositoryModifyUser repo){
        return  new ModifyUserUseCase(repo);
    }
}
