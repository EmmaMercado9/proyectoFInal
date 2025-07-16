package com.ema.practica.infrastructure.product.rest.api;

import com.ema.practica.infrastructure.product.useCases.DeleteProductUseCasesImp;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/deleteProduct")
@CrossOrigin("*")

public class DeleteProductController {

    private  final DeleteProductUseCasesImp deleteProductUseCasesImp;
    public DeleteProductController(DeleteProductUseCasesImp deleteProductUseCasesImp){
        this.deleteProductUseCasesImp=deleteProductUseCasesImp;
    }

    @DeleteMapping()
    public ResponseEntity<?> loginUser(@RequestBody Long product_id) throws Exception {

        boolean resultado=deleteProductUseCasesImp.deleteProduct(product_id);
        return ResponseEntity.ok().body(resultado);
    }
}
