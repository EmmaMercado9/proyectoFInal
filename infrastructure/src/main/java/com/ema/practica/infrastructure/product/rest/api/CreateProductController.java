package com.ema.practica.infrastructure.product.rest.api;

import com.ema.practica.core.product.input.IAddProductInput;

import com.ema.practica.infrastructure.images.ImageModel;
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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


@RestController
@CrossOrigin("*")

public class CreateProductController {

    IAddProductInput iAddProductInput;
    private final ListProductUseCaseImp listProductUseCaseImp;
    private final AddProductUseCaseImp addProductUseCaseImp;


    @Autowired
    public CreateProductController(IAddProductInput iAddProductInput, ListProductUseCaseImp listProductUseCaseImp, AddProductUseCaseImp addProductUseCaseImp){
        this.iAddProductInput=iAddProductInput;
        this.listProductUseCaseImp = listProductUseCaseImp;
        this.addProductUseCaseImp = addProductUseCaseImp;
    }

    @PostMapping(value = "/productsAdd/{category_id}",consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> addProduct(@PathVariable Long category_id, @RequestPart("product") ProductDTO productDTO,
                                        @RequestPart("imageFile")MultipartFile[] file) {
        System.out.println("ProductDTO: " + productDTO);
        System.out.println("Files received: " + file.length);
        try {
            Set<ImageModel> images=uploadImage(file);
            System.out.println("imagen "+images.toString());

            System.out.println("imagene en el dto antes "+productDTO.getImageMSet());
            productDTO.setImageMSet(images.stream().map(ImageModel::entityToCore).collect(Collectors.toSet()));

            System.out.println("imagene en el dto despues "+productDTO.getImageMSet());
            ProductEntity productEntity=addProductUseCaseImp.createProduct(category_id,productDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(productEntity);
        }catch (Exception e){
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }

    }

    public Set<ImageModel> uploadImage(MultipartFile[] multipartFiles) throws IOException {
        Set<ImageModel> imageModelSet=new HashSet<>();
        for(MultipartFile file:multipartFiles){
            ImageModel imageModel=new ImageModel(
            file.getOriginalFilename(),
            file.getContentType(),
            file.getBytes());
            imageModelSet.add(imageModel);
        }
        return imageModelSet;
    }
}
