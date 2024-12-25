package com.ema.practica.core.category.input;

import com.ema.practica.core.category.model.CategoryProduct;

public interface IUpdateCategoryInput {


    boolean updateCategory(String nombre,Long categoryId);
}
