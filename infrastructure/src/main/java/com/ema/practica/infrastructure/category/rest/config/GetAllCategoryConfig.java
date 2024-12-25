package com.ema.practica.infrastructure.category.rest.config;

import com.ema.practica.core.cart.input.IConsultCartInput;
import com.ema.practica.core.cart.output.IRepositoryConsultCart;
import com.ema.practica.core.cart.useCases.ConsultCartUseCase;
import com.ema.practica.core.category.input.IConsultCategoryInput;
import com.ema.practica.core.category.output.IRepositoryConsultCategory;
import com.ema.practica.core.category.useCases.ConsultCategoryUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GetAllCategoryConfig {

    @Bean
    public IConsultCategoryInput iConsultCategoryInput (IRepositoryConsultCategory repo){
        return new ConsultCategoryUseCase(repo);
    }
}
