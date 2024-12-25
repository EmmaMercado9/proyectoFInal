package com.ema.practica.infrastructure.product.crud;

import com.ema.practica.core.category.model.CategoryProduct;
import com.ema.practica.core.product.model.Product;
import com.ema.practica.infrastructure.category.entity.CategoryProductEntity;
import com.ema.practica.infrastructure.product.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface IListProductCrud extends CrudRepository<ProductEntity,Long> {

 List<ProductEntity> findByCategoryProductIdCategoryAndIdProductNot(Long id,Long idproduct);
 List<ProductEntity> findByCategoryProductIdCategory(Long id);
 List<ProductEntity> findFirstByOrderByPriceUnitaryAsc();
 Long findByIdProduct(Long idProduct);


}
