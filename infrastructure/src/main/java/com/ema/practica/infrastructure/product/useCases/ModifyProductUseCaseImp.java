package com.ema.practica.infrastructure.product.useCases;
import com.ema.practica.infrastructure.product.crud.IModifyProductCrud;
import com.ema.practica.infrastructure.product.entity.ProductEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ModifyProductUseCaseImp {

    private final IModifyProductCrud iModifyProductCrud;
    @Autowired
    public ModifyProductUseCaseImp(IModifyProductCrud iModifyProductCrud){
        this.iModifyProductCrud=iModifyProductCrud;
    }


    public boolean exists(Long id) {
        return iModifyProductCrud.existsById(id);
    }

    public ProductEntity get_Current_product(Long idCurrenProduc) {
        ProductEntity productEntity=iModifyProductCrud.findById(idCurrenProduc).orElse(null);
        assert productEntity != null;
        return productEntity;
    }

    public boolean modifyProduct(ProductEntity product, Long idCurrenProduc) {
        ProductEntity productEntity=iModifyProductCrud.findById(idCurrenProduc).orElse(null);
        if(productEntity!=null) {
            iModifyProductCrud.save(productEntity);
        return true;}
        return false;
    }

}
