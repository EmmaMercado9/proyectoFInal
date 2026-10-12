package com.ema.practica.infrastructure.moduloProduct.category.crud;

import com.ema.practica.infrastructure.moduloProduct.category.entity.CategoryProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryProductEntity, Long> {
    boolean existsByNameCategory(String nameCategory);
}