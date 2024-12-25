package com.ema.practica.infrastructure.detailOrder.rest.config;

import com.ema.practica.core.cart.input.IDeleteProductCartInput;
import com.ema.practica.core.cart.output.IRepositoryDeleteProductCart;
import com.ema.practica.core.cart.useCases.DeleteProductCartUseCase;
import com.ema.practica.core.detailOrder.input.IDeleteDetailOrderInput;
import com.ema.practica.core.detailOrder.output.IRepositoryDeleteDetailOrder;
import com.ema.practica.core.detailOrder.useCases.DeleteDetailOrderUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeleteDetailnfig {

    @Bean
    public IDeleteDetailOrderInput iDeleteDetailOrderInput(IRepositoryDeleteDetailOrder iRepositoryDeleteDetailOrder){
        return  new DeleteDetailOrderUseCase(iRepositoryDeleteDetailOrder);
    }

}
