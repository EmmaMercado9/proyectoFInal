package com.ema.practica.infrastructure.product.useCases;
import com.ema.practica.core.product.model.Product;
import com.ema.practica.core.product.output.IRepositoryModifiProduct;
import com.ema.practica.infrastructure.product.crud.IModifyProductCrud;
import com.ema.practica.infrastructure.product.entity.ProductEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ModifyProductUseCaseImp implements IRepositoryModifiProduct{

    private final IModifyProductCrud iModifyProductCrud;
    @Autowired
    public ModifyProductUseCaseImp(IModifyProductCrud iModifyProductCrud){
        this.iModifyProductCrud=iModifyProductCrud;
    }

    @Override
    public boolean exists(Long id) {
        return iModifyProductCrud.existsById(id);
    }

    @Override
    public Product get_Current_product(Long idCurrenProduc) {
        ProductEntity productEntity=iModifyProductCrud.findById(idCurrenProduc).orElse(null);
        assert productEntity != null;
        return productEntity.entityToCore();
    }

    @Override
    public boolean modifyProduct(Product product, Long idCurrenProduc) {
        ProductEntity productEntity=iModifyProductCrud.findById(idCurrenProduc).orElse(null);
        if(productEntity!=null) {
            ProductEntity updatedProductEntity = ProductEntity.coreToEntity(product);
            iModifyProductCrud.save(updatedProductEntity);
        return true;}
        return false;
    }

}
