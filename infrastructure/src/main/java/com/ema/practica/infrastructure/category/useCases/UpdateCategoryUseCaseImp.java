package com.ema.practica.infrastructure.category.useCases;

import com.ema.practica.core.category.model.CategoryProduct;
import com.ema.practica.core.category.output.IRepositoryUpdateCategory;
import com.ema.practica.infrastructure.category.crud.IUpdateCategoryCrud;
import com.ema.practica.infrastructure.category.entity.CategoryProductEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UpdateCategoryUseCaseImp implements IRepositoryUpdateCategory {

    IUpdateCategoryCrud iUpdateCategoryCrud;
    @Autowired
    public UpdateCategoryUseCaseImp(IUpdateCategoryCrud iUpdateCategoryCrud) {
        this.iUpdateCategoryCrud = iUpdateCategoryCrud;
    }
    @Override
    public CategoryProduct categoryCurrent(Long id) {
        CategoryProductEntity categoryProductEntity=iUpdateCategoryCrud.findById(id).orElse(null);
        assert categoryProductEntity != null;
        return (categoryProductEntity.entityToCore());
    }

    @Override
    public boolean updateCategory(CategoryProduct category, Long idCategori) {
        CategoryProductEntity categoryProductEntity = iUpdateCategoryCrud.findById(idCategori).orElse(null);
        if (categoryProductEntity != null) {
            CategoryProductEntity updatedProductEntity = CategoryProductEntity.coreToEntity(category);
            iUpdateCategoryCrud.save(updatedProductEntity);
            return true;
        }
        return false;
    }

}
