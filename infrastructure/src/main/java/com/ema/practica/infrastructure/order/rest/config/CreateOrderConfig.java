package com.ema.practica.infrastructure.order.rest.config;

import com.ema.practica.core.cart.input.IAddProductCartInput;
import com.ema.practica.core.cart.output.IRepositoryAddProductCart;
import com.ema.practica.core.cart.useCases.AddProductCartUseCase;
import com.ema.practica.core.order.input.ICreateOrderInput;
import com.ema.practica.core.order.output.IRepositoryCreateOrder;
import com.ema.practica.core.order.useCases.CreateOrderUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CreateOrderConfig {

    @Bean
    public ICreateOrderInput iCreateOrderInput(IRepositoryCreateOrder repo){
        return new CreateOrderUseCase(repo);

    }
}
