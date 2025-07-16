package com.ema.practica.infrastructure.category.crud;

import com.ema.practica.infrastructure.category.entity.CategoryProductEntity;
import com.ema.practica.infrastructure.product.entity.ProductEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface IConsultCategoryCrud extends CrudRepository<CategoryProductEntity,Long> {

}
