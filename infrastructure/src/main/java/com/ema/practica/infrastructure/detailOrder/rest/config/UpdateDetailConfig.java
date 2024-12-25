package com.ema.practica.infrastructure.detailOrder.rest.config;

import com.ema.practica.core.detailOrder.input.IUpdateDetailOrderInput;
import com.ema.practica.core.detailOrder.output.IRepositoryUpdateDetailOrder;
import com.ema.practica.core.detailOrder.useCases.UpdateDetailOrderUseCase;
import com.ema.practica.core.order.input.IUpdateOrderStatusInput;
import com.ema.practica.core.order.output.IRepositoryUpdateOrder;
import com.ema.practica.core.order.useCases.UpdateOrderUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UpdateDetailConfig {

    @Bean
    public IUpdateDetailOrderInput iUpdateDetailOrderInput(IRepositoryUpdateDetailOrder iRepositoryUpdateDetailOrder){
        return  new UpdateDetailOrderUseCase(iRepositoryUpdateDetailOrder);
    }

}
