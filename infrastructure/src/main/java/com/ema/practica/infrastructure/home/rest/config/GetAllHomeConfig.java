package com.ema.practica.infrastructure.home.rest.config;

import com.ema.practica.core.cart.input.IConsultCartInput;
import com.ema.practica.core.cart.output.IRepositoryConsultCart;
import com.ema.practica.core.cart.useCases.ConsultCartUseCase;
import com.ema.practica.core.home.input.IConsultHomeInput;
import com.ema.practica.core.home.output.IRepositoryConsultHome;
import com.ema.practica.core.home.useCases.ConsultHomeUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GetAllHomeConfig {

    @Bean
    public IConsultHomeInput iConsultHomeInput (IRepositoryConsultHome repo){
        return new ConsultHomeUseCase(repo);
    }
}
