package com.ema.practica.core.category.output;

public interface IRepositoryDeleteCategory {

    boolean exists(String category);
    boolean deleteCategory(Long idcategory);
}
