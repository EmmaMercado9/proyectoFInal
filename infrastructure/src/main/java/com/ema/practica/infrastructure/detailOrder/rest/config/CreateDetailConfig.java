package com.ema.practica.infrastructure.detailOrder.rest.config;

import com.ema.practica.core.cart.input.IAddProductCartInput;
import com.ema.practica.core.cart.output.IRepositoryAddProductCart;
import com.ema.practica.core.cart.useCases.AddProductCartUseCase;
import com.ema.practica.core.detailOrder.input.ICreateDetailOrderInput;
import com.ema.practica.core.detailOrder.output.IRepositoryCreateDetailOrde;
import com.ema.practica.core.detailOrder.useCases.CreateDetailUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CreateDetailConfig {

    @Bean
    public ICreateDetailOrderInput iCreateDetailOrderInput(IRepositoryCreateDetailOrde repo){
        return new CreateDetailUseCase(repo);

    }
}
