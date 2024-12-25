package com.ema.practica.infrastructure.category.crud;

import com.ema.practica.infrastructure.category.entity.CategoryProductEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IUpdateCategoryCrud extends CrudRepository<CategoryProductEntity,Long> {

}
