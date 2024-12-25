package com.ema.practica.infrastructure.product.rest.config;

import com.ema.practica.core.product.input.IAddProductInput;
import com.ema.practica.core.product.output.IRepositoryAddProuct;
import com.ema.practica.core.product.usecases.AddProductUseCase;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CreateProductConfig {

    @Bean
    public IAddProductInput AddProduct(IRepositoryAddProuct repo){
        return new AddProductUseCase(repo);

    }
}
