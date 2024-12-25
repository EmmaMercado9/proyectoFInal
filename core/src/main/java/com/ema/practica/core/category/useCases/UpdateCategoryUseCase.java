package com.ema.practica.core.category.useCases;

import com.ema.practica.core.category.input.IUpdateCategoryInput;
import com.ema.practica.core.category.model.CategoryProduct;
import com.ema.practica.core.category.output.IRepositoryUpdateCategory;

public class UpdateCategoryUseCase  implements IUpdateCategoryInput {

    IRepositoryUpdateCategory iRepositoryUpdateCategory;
    public UpdateCategoryUseCase(IRepositoryUpdateCategory iRepositoryUpdateCategory){
        this.iRepositoryUpdateCategory=iRepositoryUpdateCategory;
    }
    @Override
    public boolean updateCategory(String nombre, Long categoryId) {
        CategoryProduct categoryProduct=iRepositoryUpdateCategory.categoryCurrent(categoryId);
        categoryProduct.setNameCategory(nombre);
        return iRepositoryUpdateCategory.updateCategory(categoryProduct,categoryId);
    }
}
