package com.ema.practica.infrastructure.category.useCases;

import com.ema.practica.core.category.model.CategoryProduct;
import com.ema.practica.core.category.output.IRepositoryAddCategory;
import com.ema.practica.infrastructure.category.crud.IAddCategoryCrud;
import com.ema.practica.infrastructure.category.entity.CategoryProductEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class AddCategoryUseCaseImp implements IRepositoryAddCategory {
    IAddCategoryCrud iAddCategoryCrud;
    @Autowired
    public AddCategoryUseCaseImp(IAddCategoryCrud iAddCategoryCrud) {
        this.iAddCategoryCrud = iAddCategoryCrud;
    }

    @Override
    public boolean exists(String namecategory) {
        return iAddCategoryCrud.existsByNameCategory(namecategory);
    }

    @Override
    public boolean save(CategoryProduct category) {
        CategoryProductEntity categoryProductEntity= CategoryProductEntity.coreToEntity(category);
        return iAddCategoryCrud.save(categoryProductEntity).getIdCategory()!=null;
    }
}
