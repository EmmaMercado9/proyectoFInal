package com.ema.practica.infrastructure.product.useCases;

import com.ema.practica.core.product.output.IRepositoryDeleteProduct;
import com.ema.practica.infrastructure.product.crud.IDeleteProductCrud;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeleteProductUseCasesImp implements IRepositoryDeleteProduct {
    private final IDeleteProductCrud iDeleteProductCrud;
   @Autowired
    public  DeleteProductUseCasesImp(IDeleteProductCrud iDeleteProductCrud){
       this.iDeleteProductCrud=iDeleteProductCrud;
   }


    @Override
    public boolean exists(Long id) {
        return iDeleteProductCrud.existsById(id);
    }

    @Override
    public boolean deleteProduct(Long productId) {
       iDeleteProductCrud.deleteById(productId);
       return true;
    }
}
