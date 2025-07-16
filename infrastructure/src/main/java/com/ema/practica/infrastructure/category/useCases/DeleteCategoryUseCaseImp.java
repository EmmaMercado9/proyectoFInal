package com.ema.practica.infrastructure.category.useCases;

import com.ema.practica.infrastructure.category.crud.IDeleteCategoryCrud;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeleteCategoryUseCaseImp {

    IDeleteCategoryCrud iDeleteCategoryCrud;
    @Autowired
    public DeleteCategoryUseCaseImp(IDeleteCategoryCrud iDeleteCategoryCrud) {
        this.iDeleteCategoryCrud = iDeleteCategoryCrud;
    }
    public boolean exists(Long id) {
        return iDeleteCategoryCrud.existsById(id);
    }


    public boolean deleteCategory(Long idcategory) {
        iDeleteCategoryCrud.deleteById(idcategory);
        return true;
    }
}
