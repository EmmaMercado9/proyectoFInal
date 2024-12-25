package com.ema.practica.infrastructure.category.useCases;

import com.ema.practica.core.category.input.IDeleteCategoryInput;
import com.ema.practica.core.category.model.CategoryProduct;
import com.ema.practica.core.category.output.IRepositoryDeleteCategory;
import com.ema.practica.infrastructure.category.crud.IDeleteCategoryCrud;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeleteCategoryUseCaseImp implements IRepositoryDeleteCategory {

    IDeleteCategoryCrud iDeleteCategoryCrud;
    @Autowired
    public DeleteCategoryUseCaseImp(IDeleteCategoryCrud iDeleteCategoryCrud) {
        this.iDeleteCategoryCrud = iDeleteCategoryCrud;
    }
    @Override
    public boolean exists(String namecategory) {
        return iDeleteCategoryCrud.existsByNameCategory(namecategory);
    }

    @Override
    public boolean deleteCategory(Long idcategory) {
        iDeleteCategoryCrud.deleteById(idcategory);
        return true;
    }
}
