package com.ema.practica.infrastructure.home.rest.config;

import com.ema.practica.core.cart.input.IDeleteProductCartInput;
import com.ema.practica.core.cart.output.IRepositoryDeleteProductCart;
import com.ema.practica.core.cart.useCases.DeleteProductCartUseCase;
import com.ema.practica.core.home.input.IDeleteHomeInput;
import com.ema.practica.core.home.output.IRepositoryDeleteHome;
import com.ema.practica.core.home.useCases.DeleteHomeUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeleteHomenfig {

    @Bean
    public IDeleteHomeInput iDeleteHomeInput(IRepositoryDeleteHome iRepositoryDeleteHome){
        return  new DeleteHomeUseCase(iRepositoryDeleteHome);
    }

}
