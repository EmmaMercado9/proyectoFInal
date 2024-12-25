package com.ema.practica.infrastructure.cart.rest.config;

import com.ema.practica.core.cart.input.IConsultCartInput;
import com.ema.practica.core.cart.output.IRepositoryConsultCart;
import com.ema.practica.core.cart.useCases.ConsultCartUseCase;
import com.ema.practica.core.product.input.IListProductInput;
import com.ema.practica.core.product.output.IRepositoryListProduct;
import com.ema.practica.core.product.usecases.ListProductUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GetAllCartConfig {

    @Bean
    public IConsultCartInput iConsultCartInput (IRepositoryConsultCart repo){
        return new ConsultCartUseCase(repo);
    }
}
