package com.ema.practica.infrastructure.category.useCases;

import com.ema.practica.infrastructure.category.crud.IAddCategoryCrud;
import com.ema.practica.infrastructure.category.entity.CategoryProductEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class AddCategoryUseCaseImp {
    IAddCategoryCrud iAddCategoryCrud;
    @Autowired
    public AddCategoryUseCaseImp(IAddCategoryCrud iAddCategoryCrud) {
        this.iAddCategoryCrud = iAddCategoryCrud;
    }

    public boolean exists(String namecategory) {
        return iAddCategoryCrud.existsByNameCategory(namecategory);
    }

    public boolean save(CategoryProductEntity category) {
        return iAddCategoryCrud.save(category).getIdCategory()!=null;
    }
}
