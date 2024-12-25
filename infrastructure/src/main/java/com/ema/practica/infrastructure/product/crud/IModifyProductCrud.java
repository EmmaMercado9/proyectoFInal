package com.ema.practica.infrastructure.product.crud;

import com.ema.practica.core.category.model.CategoryProduct;
import com.ema.practica.infrastructure.product.entity.ProductEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IModifyProductCrud extends CrudRepository<ProductEntity,Long> {

}
