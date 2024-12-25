package com.ema.practica.infrastructure.product.rest.config;


import com.ema.practica.core.product.input.IModifyProductInput;
import com.ema.practica.core.product.output.IRepositoryModifiProduct;
import com.ema.practica.core.product.usecases.ModifyProductUseCase;
import com.ema.practica.core.user.input.IModifyUserInput;
import com.ema.practica.core.user.output.IRepositoryModifyUser;
import com.ema.practica.core.user.usecases.ModifyUserUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModifyProductConfig {
    @Bean
    public IModifyProductInput iModifyProductInput(IRepositoryModifiProduct repo){
        return  new ModifyProductUseCase(repo);
    }
}
