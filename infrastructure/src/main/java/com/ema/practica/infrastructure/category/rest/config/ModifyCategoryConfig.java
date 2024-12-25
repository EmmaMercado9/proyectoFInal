package com.ema.practica.infrastructure.category.rest.config;


import com.ema.practica.core.category.input.IUpdateCategoryInput;
import com.ema.practica.core.category.output.IRepositoryUpdateCategory;
import com.ema.practica.core.category.useCases.UpdateCategoryUseCase;
import com.ema.practica.core.product.input.IModifyProductInput;
import com.ema.practica.core.product.output.IRepositoryModifiProduct;
import com.ema.practica.core.product.usecases.ModifyProductUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModifyCategoryConfig {
    @Bean
    public IUpdateCategoryInput iUpdateCategoryInput(IRepositoryUpdateCategory repo){
        return  new UpdateCategoryUseCase(repo);
    }
}
