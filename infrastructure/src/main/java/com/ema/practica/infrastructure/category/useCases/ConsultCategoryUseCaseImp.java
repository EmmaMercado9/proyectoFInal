package com.ema.practica.infrastructure.category.useCases;

import com.ema.practica.infrastructure.category.crud.IConsultCategoryCrud;
import com.ema.practica.infrastructure.category.entity.CategoryProductEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class ConsultCategoryUseCaseImp {

    IConsultCategoryCrud iConsultCategoryCrud;
    @Autowired
    public ConsultCategoryUseCaseImp(IConsultCategoryCrud iConsultCategoryCrud){
        this.iConsultCategoryCrud=iConsultCategoryCrud;
    }

    public Collection<CategoryProductEntity> getCategoryProducts() {
        Iterable<CategoryProductEntity> categoryProductEntities = iConsultCategoryCrud.findAll();
        return StreamSupport.stream(categoryProductEntities.spliterator(), false).collect(Collectors.toList());
    }


}
