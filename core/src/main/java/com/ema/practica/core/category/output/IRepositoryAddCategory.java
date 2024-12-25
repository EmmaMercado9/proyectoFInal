package com.ema.practica.core.category.output;

import com.ema.practica.core.category.model.CategoryProduct;

public interface IRepositoryAddCategory {

    boolean exists(String category);

    boolean save(CategoryProduct category);
}
