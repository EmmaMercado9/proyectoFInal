package com.ema.practica.infrastructure.product.rest.api;

import com.ema.practica.core.category.model.CategoryProduct;
import com.ema.practica.core.product.input.IDeleteProductInput;
import com.ema.practica.core.product.model.Product;

import com.ema.practica.infrastructure.product.rest.model.ProductDTO;
import com.ema.practica.infrastructure.product.useCases.ListProductUseCaseImp;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/deleteProduct")
@CrossOrigin("*")

public class DeleteProductController {

    private  final IDeleteProductInput iDeleteProductInput;
    public DeleteProductController(IDeleteProductInput iDeleteProductInput){
        this.iDeleteProductInput=iDeleteProductInput;
    }

    @DeleteMapping()
    public ResponseEntity<?> loginUser(@RequestBody Long product_id) throws Exception {

        boolean resultado=iDeleteProductInput.deleteProduct(product_id);
        return ResponseEntity.ok().body(resultado);
    }
}
