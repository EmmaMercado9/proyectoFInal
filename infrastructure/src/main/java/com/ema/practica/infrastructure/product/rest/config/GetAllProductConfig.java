package com.ema.practica.infrastructure.product.rest.config;

import com.ema.practica.core.product.input.IListProductInput;
import com.ema.practica.core.product.output.IRepositoryListProduct;
import com.ema.practica.core.product.usecases.ListProductUseCase;
import com.ema.practica.core.user.input.IListUserInput;
import com.ema.practica.core.user.output.IRepositoryListUser;
import com.ema.practica.core.user.usecases.ListUserUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GetAllProductConfig {

    @Bean
    public IListProductInput IListProduct (IRepositoryListProduct repo){
        return new ListProductUseCase(repo);
    }
}
