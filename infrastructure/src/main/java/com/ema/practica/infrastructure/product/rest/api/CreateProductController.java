package com.ema.practica.infrastructure.product.rest.api;

import com.ema.practica.infrastructure.product.entity.ProductEntity;
import com.ema.practica.infrastructure.product.rest.model.ProductDTO;
import com.ema.practica.infrastructure.product.useCases.AddProductUseCaseImp;
import com.ema.practica.infrastructure.product.useCases.ListProductUseCaseImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@CrossOrigin("*")

public class CreateProductController {

    private final AddProductUseCaseImp addProductUseCaseImp;


    @Autowired
    public CreateProductController(ListProductUseCaseImp listProductUseCaseImp, AddProductUseCaseImp addProductUseCaseImp){
        this.addProductUseCaseImp = addProductUseCaseImp;
    }

    @PostMapping(value = "/productsAdd/{category_id}")
    public ResponseEntity<?> addProduct(@PathVariable Long category_id, @RequestPart("product") ProductDTO productDTO,
                                        @RequestPart("imageFile")MultipartFile[] file) {
        System.out.println("ProductDTO: " + productDTO);

        System.out.println("Files received: " + file.length);
        try {

            System.out.println("imagene en el dto despues "+productDTO.getImageMSet());
            ProductEntity productEntity=addProductUseCaseImp.createProduct(category_id,productDTO,file);
            System.out.println("imagene en el dto despues "+productDTO.getImageMSet());

            return ResponseEntity.status(HttpStatus.CREATED).body(productEntity);
        }catch (Exception e){
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }

    }


}
