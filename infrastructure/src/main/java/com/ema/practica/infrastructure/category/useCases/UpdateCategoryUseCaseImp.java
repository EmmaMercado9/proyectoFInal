package com.ema.practica.infrastructure.category.useCases;

import com.ema.practica.infrastructure.category.crud.IUpdateCategoryCrud;
import com.ema.practica.infrastructure.category.entity.CategoryProductEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UpdateCategoryUseCaseImp {

    IUpdateCategoryCrud iUpdateCategoryCrud;
    @Autowired
    public UpdateCategoryUseCaseImp(IUpdateCategoryCrud iUpdateCategoryCrud) {
        this.iUpdateCategoryCrud = iUpdateCategoryCrud;
    }
    public CategoryProductEntity categoryCurrent(Long id) {
        CategoryProductEntity categoryProductEntity=iUpdateCategoryCrud.findById(id).orElse(null);
        assert categoryProductEntity != null;
        return (categoryProductEntity);
    }

    public boolean updateCategory(String category, Long idCategori) {
        CategoryProductEntity categoryProductEntity = iUpdateCategoryCrud.findById(idCategori).orElse(null);
        if (categoryProductEntity != null) {
            categoryProductEntity.setNameCategory(category);
            iUpdateCategoryCrud.save(categoryProductEntity);
            return true;
        }
        return false;
    }

}
