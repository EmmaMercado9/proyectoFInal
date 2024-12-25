package com.ema.practica.infrastructure.product.useCases;

import com.ema.practica.core.category.model.CategoryProduct;
import com.ema.practica.core.product.model.Product;
import com.ema.practica.core.product.output.IRepositoryListProduct;
import com.ema.practica.infrastructure.category.crud.IConsultCategoryCrud;
import com.ema.practica.infrastructure.category.entity.CategoryProductEntity;
import com.ema.practica.infrastructure.product.crud.IListProductCrud;
import com.ema.practica.infrastructure.product.entity.ProductEntity;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Transactional

public class ListProductUseCaseImp implements IRepositoryListProduct {

    private  final IListProductCrud iListProductCrud;
    private  final IConsultCategoryCrud iConsultCategoryCrud;
    @Autowired
    public ListProductUseCaseImp(IListProductCrud iListProductCrud, IConsultCategoryCrud iConsultCategoryCrud) {
        this.iListProductCrud = iListProductCrud;
        this.iConsultCategoryCrud = iConsultCategoryCrud;
    }

    @Override
    public Collection<Product> getProducts() {
        Iterable<ProductEntity> productEntities = this.iListProductCrud.findAll();
        return StreamSupport.stream(productEntities.spliterator(), false).map(
                ProductEntity::entityToCore).collect(Collectors.toList());
    }


    public List<ProductEntity> getRelatedProducts(Long idcategory,Long idProduct){
        List<ProductEntity> productEntities=this.iListProductCrud.findByCategoryProductIdCategoryAndIdProductNot(idcategory,idProduct);

        List<ProductEntity > ramdonProductList=new ArrayList<>();
        Random random=new Random();
        for (int i=0;i<2;i++){
            int randonIndex=random.nextInt(productEntities.size());
            ramdonProductList.add(productEntities.get(randonIndex));
            productEntities.remove(randonIndex);
        }
    return  ramdonProductList;
    }
    public List<ProductEntity> getBestPriceUnitary(){
        return this.iListProductCrud.findFirstByOrderByPriceUnitaryAsc();
    }

    public Optional<ProductEntity> getProductId(Long id){
        return this.iListProductCrud.findById(id);
    }

    public CategoryProduct getCategoryId(Long id) {
        Optional<CategoryProductEntity> optionalCategoryProduct=this.iConsultCategoryCrud.findById(id);
        if(optionalCategoryProduct.isEmpty()){
            throw new RuntimeException("No  existe category ");
        }
        else {
            return optionalCategoryProduct.get().entityToCore();
        }

    }

    public Product getProId(Long id){
        Optional<ProductEntity>optionalProductEntity= this.iListProductCrud.findById(id);
        if(optionalProductEntity.isEmpty()){
            throw new RuntimeException("Product no encontrado");
        }
        return optionalProductEntity.get().entityToCore();
    }

    public List<Product> getProductsByIdCategory(Long id){
      List<ProductEntity> productEntities =this.iListProductCrud.findByCategoryProductIdCategory(id);
        return productEntities.stream().map(ProductEntity::entityToCore).toList();
    }

}
