package com.ema.practica.infrastructure.product.useCases;

import com.ema.practica.infrastructure.category.crud.IConsultCategoryCrud;
import com.ema.practica.infrastructure.category.entity.CategoryProductEntity;
import com.ema.practica.infrastructure.images.ImageModel;
import com.ema.practica.infrastructure.images.services.ImagenService;
import com.ema.practica.infrastructure.product.crud.IAddProductCrud;
import com.ema.practica.infrastructure.product.crud.IListProductCrud;
import com.ema.practica.infrastructure.product.entity.ProductEntity;
import com.ema.practica.infrastructure.product.rest.model.ProductDTO;
import com.ema.practica.infrastructure.sizes.SizeEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AddProductUseCaseImp {

    private final IAddProductCrud iAddProductCrud;
    private final ListProductUseCaseImp listProductUseCaseImp;
    private final IConsultCategoryCrud iConsultCategoryCrud;
    private final ImagenService imagenService;
    @Autowired
    public AddProductUseCaseImp(IAddProductCrud iAddProductCrud, ListProductUseCaseImp listProductUseCaseImp, IConsultCategoryCrud iConsultCategoryCrud ,ImagenService imagenService) {
        this.iAddProductCrud=iAddProductCrud;
        this.listProductUseCaseImp=listProductUseCaseImp;
        this.iConsultCategoryCrud = iConsultCategoryCrud;
        this.imagenService = imagenService;
    }

    public boolean SaveUser(ProductEntity productEntity) {
        CategoryProductEntity categoryProductEntity = listProductUseCaseImp.getCategoryId(productEntity.getCategoryProduct().getIdCategory());
        if (categoryProductEntity == null) {
            throw  new RuntimeException("Category no encontrada");
        }
        productEntity.setCategoryProduct(categoryProductEntity);
        return iAddProductCrud.save(productEntity).getIdProduct()!=null;
    }
    public ProductEntity createProduct(Long id, ProductDTO product, MultipartFile[] files) {
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
            if (files != null && files.length > 0) {
                Set<ImageModel> images = Arrays.stream(files)
                        .filter(file -> file != null && !file.isEmpty())
                        .map(file -> {
                            try {

                                        ImageModel image=imagenService.uploadImage(file); // Método para subir la imagen
                                image.setProduct(productEntity); // Asegurar la relación

                                return image;
                            } catch (IOException e) {
                                throw new RuntimeException("Error al subir imagen", e);
                            }
                        })
                        .collect(Collectors.toSet());

                productEntity.getImageModelSet().addAll(images);
            }
            return iAddProductCrud.save(productEntity);

        }
        return null;
     }


}
