package com.ema.practica.infrastructure.user.rest.config;

import com.ema.practica.core.product.input.IDeleteProductInput;
import com.ema.practica.core.product.output.IRepositoryDeleteProduct;
import com.ema.practica.core.product.usecases.DeleteProductUseCases;
import com.ema.practica.core.user.input.IDeleteUserInput;
import com.ema.practica.core.user.output.IRepositoryDeleteUser;
import com.ema.practica.core.user.usecases.DeleteUserUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeleteUserConfig {

    @Bean
    public IDeleteUserInput iDeleteUserInput(IRepositoryDeleteUser iRepositoryDeleteUser){
        return  new DeleteUserUseCase(iRepositoryDeleteUser);
    }

}
