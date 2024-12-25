package com.ema.practica.core.product.usecases;

import com.ema.practica.core.category.model.CategoryProduct;
import com.ema.practica.core.images.ImageM;
import com.ema.practica.core.product.input.IModifyProductInput;
import com.ema.practica.core.product.model.Product;
import com.ema.practica.core.product.output.IRepositoryModifiProduct;
import com.ema.practica.core.sizes.Size;

import java.util.List;
import java.util.Set;

public class ModifyProductUseCase implements IModifyProductInput {

    IRepositoryModifiProduct iRepositoryModifiProduct;
    public ModifyProductUseCase (IRepositoryModifiProduct iRepositoryModifiProduct){
        this.iRepositoryModifiProduct = iRepositoryModifiProduct;  // Inyectamos la dependencia al repositorio de modificación de producto.  // La inyección de dependencia es el patrón de diseño de software que permite desacoplar el código de las interfaces de las clases que las implementan.

    }

    @Override
    public boolean modifyProduct(CategoryProduct category, String nameProduct, double priceUnitary, double discontuedPrice, String descriptionProduct, boolean status, String color, List<Size> sizeList, Set<ImageM> imageMSet, Long idCurrent) {
        Product product=iRepositoryModifiProduct.get_Current_product(idCurrent);

        product.setCategory(category);
        product.setNameProduct(nameProduct);
        product.setPriceUnitary(priceUnitary);
        product.setDescriptionProduct(descriptionProduct);
        product.setStatus(status);
        product.setColor(color);
        product.setSizes(sizeList);
        product.setImageMSet(imageMSet);

        return iRepositoryModifiProduct.modifyProduct(product,idCurrent);
    }
}
