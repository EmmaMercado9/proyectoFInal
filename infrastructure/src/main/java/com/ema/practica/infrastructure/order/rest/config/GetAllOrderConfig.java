package com.ema.practica.infrastructure.order.rest.config;

import com.ema.practica.core.cart.input.IConsultCartInput;
import com.ema.practica.core.cart.output.IRepositoryConsultCart;
import com.ema.practica.core.cart.useCases.ConsultCartUseCase;
import com.ema.practica.core.order.input.IConsultOrderInput;
import com.ema.practica.core.order.output.IRepositoryConsultOrder;
import com.ema.practica.core.order.useCases.ConsutlOrderUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GetAllOrderConfig {

    @Bean
    public IConsultOrderInput iConsultOrderInput (IRepositoryConsultOrder repo){
        return new ConsutlOrderUseCase(repo);
    }
}
