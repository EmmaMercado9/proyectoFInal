package com.ema.practica.infrastructure.category.useCases;

import com.ema.practica.core.category.model.CategoryProduct;
import com.ema.practica.core.category.output.IRepositoryConsultCategory;
import com.ema.practica.infrastructure.category.crud.IConsultCategoryCrud;
import com.ema.practica.infrastructure.category.entity.CategoryProductEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class ConsultCategoryUseCaseImp implements IRepositoryConsultCategory {

    IConsultCategoryCrud iConsultCategoryCrud;
    @Autowired
    public ConsultCategoryUseCaseImp(IConsultCategoryCrud iConsultCategoryCrud){
        this.iConsultCategoryCrud=iConsultCategoryCrud;
    }

    @Override
    public Collection<CategoryProduct> getCategoryProducts() {
        Iterable<CategoryProductEntity> categoryProductEntities = iConsultCategoryCrud.findAll();
        return StreamSupport.stream(categoryProductEntities.spliterator(), false).map(CategoryProductEntity::entityToCore).collect(Collectors.toList());
    }


}
