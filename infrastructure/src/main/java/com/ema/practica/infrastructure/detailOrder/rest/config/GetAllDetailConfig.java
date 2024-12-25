package com.ema.practica.infrastructure.detailOrder.rest.config;

import com.ema.practica.core.cart.input.IConsultCartInput;
import com.ema.practica.core.cart.output.IRepositoryConsultCart;
import com.ema.practica.core.cart.useCases.ConsultCartUseCase;
import com.ema.practica.core.detailOrder.input.IConsultDetailOrderInput;
import com.ema.practica.core.detailOrder.output.IRepositoryConsultDetail;
import com.ema.practica.core.detailOrder.useCases.ConsultDetailUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GetAllDetailConfig {

    @Bean
    public IConsultDetailOrderInput iConsultDetailOrderInput (IRepositoryConsultDetail repo){
        return new ConsultDetailUseCase(repo);
    }
}
