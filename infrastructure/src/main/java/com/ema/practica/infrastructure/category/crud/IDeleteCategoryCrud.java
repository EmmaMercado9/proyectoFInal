package com.ema.practica.infrastructure.category.crud;

import com.ema.practica.core.category.model.CategoryProduct;
import com.ema.practica.infrastructure.category.entity.CategoryProductEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IDeleteCategoryCrud  extends CrudRepository<CategoryProductEntity,Long> {

    boolean existsByNameCategory(String namecategory);
}
