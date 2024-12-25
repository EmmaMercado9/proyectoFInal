package com.ema.practica.infrastructure.product.useCases;
import com.ema.practica.core.category.model.CategoryProduct;
import com.ema.practica.core.product.model.Product;
import com.ema.practica.core.product.output.IRepositoryAddProuct;
import com.ema.practica.infrastructure.category.crud.IConsultCategoryCrud;
import com.ema.practica.infrastructure.category.entity.CategoryProductEntity;
import com.ema.practica.infrastructure.images.ImageModel;
import com.ema.practica.infrastructure.product.crud.IAddProductCrud;
import com.ema.practica.infrastructure.product.crud.IListProductCrud;
import com.ema.practica.infrastructure.product.entity.ProductEntity;
import com.ema.practica.infrastructure.product.rest.model.ProductDTO;
import com.ema.practica.infrastructure.sizes.SizeEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AddProductUseCaseImp implements IRepositoryAddProuct {

    private final IAddProductCrud iAddProductCrud;
    private final IListProductCrud iListProductCrud;
    private final ListProductUseCaseImp listProductUseCaseImp;
    private final IConsultCategoryCrud iConsultCategoryCrud;
    @Autowired
    public AddProductUseCaseImp(IAddProductCrud iAddProductCrud, IListProductCrud iListProductCrud, ListProductUseCaseImp listProductUseCaseImp, IConsultCategoryCrud iConsultCategoryCrud) {
        this.iAddProductCrud=iAddProductCrud;
        this.iListProductCrud=iListProductCrud;
        this.listProductUseCaseImp=listProductUseCaseImp;
        this.iConsultCategoryCrud = iConsultCategoryCrud;
    }

    @Override
    public boolean SaveUser(Product product) {
        CategoryProduct categoryProduct = listProductUseCaseImp.getCategoryId(product.getCategory().getIdCategory());
        if (categoryProduct == null) {
            throw  new RuntimeException("Category no encontrada");
        }
        product.setCategory(categoryProduct);
        ProductEntity productEntity= ProductEntity.coreToEntity(product);
        return iAddProductCrud.save(productEntity).getIdProduct()!=null;
    }
    public ProductEntity createProduct(Long id, ProductDTO product) {
        Optional<CategoryProductEntity> categoryProduct=iConsultCategoryCrud.findById(id);
        if (categoryProduct.isPresent()) {
            ProductEntity productEntity=new ProductEntity();
            productEntity.setCategoryProduct(categoryProduct.get());
            productEntity.setNameProduct(product.getNameProduct());
            productEntity.setPriceUnitary(product.getPriceUnitary());
            productEntity.setDiscontuedPrice(product.getDiscontuedPrice());
            productEntity.setDescriptionProduct(product.getDescriptionProduct());
            productEntity.setStatus(product.isStatus());
            productEntity.setColor(product.getColor());
            List<SizeEntity> sizes = product.getListSize().stream()
                    .map(sizeDTO -> {
                        SizeEntity sizeEntity = new SizeEntity();
                        sizeEntity.setSize(sizeDTO.getSize()); // Assuming 'size' property exists
                        sizeEntity.setStock(sizeDTO.getStock()); // Assuming 'stock' property exists
                        sizeEntity.setAvailable(sizeDTO.isAvailable()); // Assuming 'available' property exists
                        return sizeEntity;
                    })
                    .toList();

            productEntity.setSizes(sizes);
            productEntity.setImageModelSet(product.getImageMSet().stream().map(ImageModel::coreToEntity).collect(Collectors.toSet()));
            return iAddProductCrud.save(productEntity);

        }
        return null;
     }


}
