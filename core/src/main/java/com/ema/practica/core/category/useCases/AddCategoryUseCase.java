package com.ema.practica.core.category.useCases;

import com.ema.practica.core.category.input.IAddCategoryInput;
import com.ema.practica.core.category.model.CategoryProduct;
import com.ema.practica.core.category.output.IRepositoryAddCategory;

public class AddCategoryUseCase implements IAddCategoryInput {
    IRepositoryAddCategory iRepositoryAddCategory;

    public AddCategoryUseCase(IRepositoryAddCategory iRepositoryAddCategory){
        this.iRepositoryAddCategory=iRepositoryAddCategory;
    }
    @Override
    public boolean addCategory(CategoryProduct categoryproduct) {
        if(!iRepositoryAddCategory.exists(categoryproduct.getNameCategory())){
            iRepositoryAddCategory.save(categoryproduct);
            return true;
        }
        return false;
    }
}
