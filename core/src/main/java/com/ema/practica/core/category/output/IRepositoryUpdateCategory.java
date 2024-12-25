package com.ema.practica.core.category.output;

import com.ema.practica.core.category.model.CategoryProduct;

public interface IRepositoryUpdateCategory {

    CategoryProduct categoryCurrent(Long id);
    boolean updateCategory(CategoryProduct category,Long idCategori);
}
