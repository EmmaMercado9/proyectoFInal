package com.ema.practica.infrastructure.order.rest.config;

import com.ema.practica.core.cart.input.IDeleteProductCartInput;
import com.ema.practica.core.cart.output.IRepositoryDeleteProductCart;
import com.ema.practica.core.cart.useCases.DeleteProductCartUseCase;
import com.ema.practica.core.order.input.IUpdateOrderStatusInput;
import com.ema.practica.core.order.output.IRepositoryUpdateOrder;
import com.ema.practica.core.order.useCases.UpdateOrderUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UpdateOrdernfig {

    @Bean
    public IUpdateOrderStatusInput iUpdateOrderStatusInput(IRepositoryUpdateOrder iRepositoryUpdateOrder){
        return  new UpdateOrderUseCase(iRepositoryUpdateOrder);
    }

}
