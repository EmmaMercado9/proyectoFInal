package com.ema.practica.core.category.useCases;

import com.ema.practica.core.category.input.IDeleteCategoryInput;
import com.ema.practica.core.category.model.CategoryProduct;
import com.ema.practica.core.category.output.IRepositoryDeleteCategory;

public class DeleteCategoryUseCase implements IDeleteCategoryInput {
    IRepositoryDeleteCategory iRepositoryDeleteCategory;

    public  DeleteCategoryUseCase(IRepositoryDeleteCategory iRepositoryDeleteCategory){
        this.iRepositoryDeleteCategory = iRepositoryDeleteCategory;
    }

    @Override
    public boolean deleteCategory(CategoryProduct category) {

        if(iRepositoryDeleteCategory.exists(category.getNameCategory())){
            iRepositoryDeleteCategory.deleteCategory(category.getIdCategory());
            return true;
        }
        return false;
    }
}
