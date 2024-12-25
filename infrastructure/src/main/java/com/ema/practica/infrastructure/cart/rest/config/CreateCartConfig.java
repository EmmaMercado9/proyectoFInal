package com.ema.practica.infrastructure.cart.rest.config;

import com.ema.practica.core.cart.input.IAddProductCartInput;
import com.ema.practica.core.cart.output.IRepositoryAddProductCart;
import com.ema.practica.core.cart.useCases.AddProductCartUseCase;
import com.ema.practica.core.product.input.IAddProductInput;
import com.ema.practica.core.product.output.IRepositoryAddProuct;
import com.ema.practica.core.product.usecases.AddProductUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CreateCartConfig {

    @Bean
    public IAddProductCartInput iAddProductCartInput(IRepositoryAddProductCart repo){
        return new AddProductCartUseCase(repo);

    }
}
