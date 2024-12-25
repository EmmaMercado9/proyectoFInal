package com.ema.practica.infrastructure.home.rest.config;

import com.ema.practica.core.home.input.IUpdateHomeInput;
import com.ema.practica.core.home.output.IRepositoryUpdateHome;
import com.ema.practica.core.home.useCases.UpdateHomeUseCase;
import com.ema.practica.core.order.input.IUpdateOrderStatusInput;
import com.ema.practica.core.order.output.IRepositoryUpdateOrder;
import com.ema.practica.core.order.useCases.UpdateOrderUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UpdateHomernfig {

    @Bean
    public IUpdateHomeInput iUpdateHomeInput(IRepositoryUpdateHome iRepositoryUpdateHome){
        return  new UpdateHomeUseCase(iRepositoryUpdateHome);
    }

}
