package com.ema.practica.core.category.output;

import com.ema.practica.core.category.model.CategoryProduct;

import java.util.Collection;

public interface IRepositoryConsultCategory {

    Collection<CategoryProduct> getCategoryProducts();
}
