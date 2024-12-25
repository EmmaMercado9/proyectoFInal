package com.ema.practica.infrastructure.home.rest.config;

import com.ema.practica.core.cart.input.IAddProductCartInput;
import com.ema.practica.core.cart.output.IRepositoryAddProductCart;
import com.ema.practica.core.cart.useCases.AddProductCartUseCase;
import com.ema.practica.core.home.input.IAddHomeInput;
import com.ema.practica.core.home.output.IRepositoryAddHome;
import com.ema.practica.core.home.useCases.AddHomeUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CreateHomeConfig {

    @Bean
    public IAddHomeInput iAddHomeInput(IRepositoryAddHome repo){
        return new AddHomeUseCase(repo);

    }
}
