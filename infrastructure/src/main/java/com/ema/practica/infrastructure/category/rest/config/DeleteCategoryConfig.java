package com.ema.practica.infrastructure.category.rest.config;

import com.ema.practica.core.cart.input.IDeleteProductCartInput;
import com.ema.practica.core.cart.output.IRepositoryDeleteProductCart;
import com.ema.practica.core.cart.useCases.DeleteProductCartUseCase;
import com.ema.practica.core.category.input.IDeleteCategoryInput;
import com.ema.practica.core.category.output.IRepositoryDeleteCategory;
import com.ema.practica.core.category.useCases.DeleteCategoryUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeleteCategoryConfig {

    @Bean
    public IDeleteCategoryInput iDeleteCategoryInput(IRepositoryDeleteCategory iRepositoryDeleteCategory){
        return  new DeleteCategoryUseCase(iRepositoryDeleteCategory);
    }

}
