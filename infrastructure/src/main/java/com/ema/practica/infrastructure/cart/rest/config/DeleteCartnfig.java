package com.ema.practica.infrastructure.cart.rest.config;

import com.ema.practica.core.cart.input.IDeleteProductCartInput;
import com.ema.practica.core.cart.output.IRepositoryDeleteProductCart;
import com.ema.practica.core.cart.useCases.DeleteProductCartUseCase;
import com.ema.practica.core.product.input.IDeleteProductInput;
import com.ema.practica.core.product.output.IRepositoryDeleteProduct;
import com.ema.practica.core.product.usecases.DeleteProductUseCases;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeleteCartnfig {

    @Bean
    public IDeleteProductCartInput iDeleteProductCartInput(IRepositoryDeleteProductCart iRepositoryDeleteProductCart){
        return  new DeleteProductCartUseCase(iRepositoryDeleteProductCart);
    }

}
