package com.ema.practica.core.product.input;

import com.ema.practica.core.category.model.CategoryProduct;
import com.ema.practica.core.images.ImageM;
import com.ema.practica.core.sizes.Size;

import java.util.List;
import java.util.Set;

public interface IModifyProductInput {

    boolean modifyProduct(CategoryProduct category, String nameProduct, double priceUnitary, double discontuedPrice, String descriptionProduct, boolean status, String color, List<Size> sizeList, Set<ImageM> imageMSet, Long id);
}
