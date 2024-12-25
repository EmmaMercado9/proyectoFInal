package com.ema.practica.core.category.useCases;

import com.ema.practica.core.category.input.IConsultCategoryInput;
import com.ema.practica.core.category.model.CategoryProduct;
import com.ema.practica.core.category.output.IRepositoryConsultCategory;

import java.util.Collection;

public class ConsultCategoryUseCase implements IConsultCategoryInput {
    IRepositoryConsultCategory iRepositoryConsultCategory;

    public  ConsultCategoryUseCase(IRepositoryConsultCategory iRepositoryConsultCategory){
        this.iRepositoryConsultCategory = iRepositoryConsultCategory;
    }
    @Override
    public Collection<CategoryProduct> getCategoryProducts() {
        return iRepositoryConsultCategory.getCategoryProducts();
    }
}
