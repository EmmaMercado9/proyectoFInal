package com.ema.practica.infrastructure.product.useCases;

import com.ema.practica.infrastructure.product.crud.IDeleteProductCrud;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeleteProductUseCasesImp{
    private final IDeleteProductCrud iDeleteProductCrud;
   @Autowired
    public  DeleteProductUseCasesImp(IDeleteProductCrud iDeleteProductCrud){
       this.iDeleteProductCrud=iDeleteProductCrud;
   }



    public boolean exists(Long id) {
        return iDeleteProductCrud.existsById(id);
    }


    public boolean deleteProduct(Long productId) {
       iDeleteProductCrud.deleteById(productId);
       return true;
    }
}
