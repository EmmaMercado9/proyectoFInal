package com.ema.practica.infrastructure.product.rest.api;

import com.ema.practica.infrastructure.product.entity.ProductEntity;
import com.ema.practica.infrastructure.product.rest.model.ProductDTO;
import com.ema.practica.infrastructure.product.useCases.ListProductUseCaseImp;
import com.ema.practica.infrastructure.product.useCases.ModifyProductUseCaseImp;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/modifyProduct")
@CrossOrigin("*")
public class ModifyProductController {
    private  final ModifyProductUseCaseImp modifyProductUseCaseImp;

    public ModifyProductController(ModifyProductUseCaseImp modifyProductUseCaseImp) {
        this.modifyProductUseCaseImp=modifyProductUseCaseImp;
    }

    @PutMapping
    public ResponseEntity<?> modifyUser(@RequestBody ProductDTO productDTO) throws Exception {
        ProductEntity product=productDTO.dtoToEntity();
        boolean result = modifyProductUseCaseImp.modifyProduct(
                product,
                product.getIdProduct());
        return ResponseEntity.ok().body(result);
    }

}
