package com.ema.practica.infrastructure.product.useCases;
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

public class ListProductUseCaseImp{

    private  final IListProductCrud iListProductCrud;
    private  final IConsultCategoryCrud iConsultCategoryCrud;
    @Autowired
    public ListProductUseCaseImp(IListProductCrud iListProductCrud, IConsultCategoryCrud iConsultCategoryCrud) {
        this.iListProductCrud = iListProductCrud;
        this.iConsultCategoryCrud = iConsultCategoryCrud;
    }


    public Collection<ProductEntity> getProducts() {
        Iterable<ProductEntity> productEntities = this.iListProductCrud.findAll();
        return StreamSupport.stream(productEntities.spliterator(), false).collect(Collectors.toList());
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

    public ProductEntity getProductId(Long id){
        return this.iListProductCrud.findById(id).get();
    }

    public CategoryProductEntity getCategoryId(Long id) {
        Optional<CategoryProductEntity> optionalCategoryProduct=this.iConsultCategoryCrud.findById(id);
        if(optionalCategoryProduct.isEmpty()){
            throw new RuntimeException("No  existe category ");
        }
        else {
            return optionalCategoryProduct.get();
        }

    }

    public List<ProductEntity> getProductsByIds(List<Long> ids){
        return  iListProductCrud.findByIdProductIn(ids).stream().toList();
    }

    public ProductEntity getProId(Long id){
        Optional<ProductEntity>optionalProductEntity= this.iListProductCrud.findById(id);
        if(optionalProductEntity.isEmpty()){
            throw new RuntimeException("Product no encontrado");
        }
        return optionalProductEntity.get();
    }

    public List<ProductEntity> getProductsByIdCategory(Long id){
      List<ProductEntity> productEntities =this.iListProductCrud.findByCategoryProductIdCategory(id);
        return productEntities.stream().toList();
    }

}
