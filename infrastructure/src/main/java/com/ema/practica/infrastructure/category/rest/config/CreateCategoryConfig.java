package com.ema.practica.infrastructure.category.rest.config;

import com.ema.practica.core.cart.input.IAddProductCartInput;
import com.ema.practica.core.cart.output.IRepositoryAddProductCart;
import com.ema.practica.core.cart.useCases.AddProductCartUseCase;
import com.ema.practica.core.category.input.IAddCategoryInput;
import com.ema.practica.core.category.output.IRepositoryAddCategory;
import com.ema.practica.core.category.useCases.AddCategoryUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CreateCategoryConfig {

    @Bean
    public IAddCategoryInput iAddCategoryInput(IRepositoryAddCategory repo){
        return new AddCategoryUseCase(repo);

    }
}
