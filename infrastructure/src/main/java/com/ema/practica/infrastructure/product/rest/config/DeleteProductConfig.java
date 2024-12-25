package com.ema.practica.infrastructure.product.rest.config;

import com.ema.practica.core.product.input.IDeleteProductInput;
import com.ema.practica.core.product.output.IRepositoryDeleteProduct;
import com.ema.practica.core.product.usecases.DeleteProductUseCases;
import com.ema.practica.core.user.input.ILoginUserInput;
import com.ema.practica.core.user.output.IRepositoryLogin;
import com.ema.practica.core.user.usecases.LoginUserUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeleteProductConfig {

    @Bean
    public IDeleteProductInput iDeleteProductInput(IRepositoryDeleteProduct iRepositoryDeleteProduct){
        return  new DeleteProductUseCases(iRepositoryDeleteProduct);
    }

}
