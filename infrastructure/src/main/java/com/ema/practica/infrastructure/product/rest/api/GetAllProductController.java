package com.ema.practica.infrastructure.product.rest.api;

import com.ema.practica.core.product.input.IListProductInput;
import com.ema.practica.core.product.model.Product;
import com.ema.practica.infrastructure.product.entity.ProductEntity;
import com.ema.practica.infrastructure.product.rest.model.ProductDTO;
import com.ema.practica.infrastructure.product.useCases.ListProductUseCaseImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin("*")
public class GetAllProductController {
    private final IListProductInput iListProductInput;
    private final ListProductUseCaseImp listProductUseCaseImp;

    @Autowired
    public GetAllProductController(IListProductInput iListProductInput, ListProductUseCaseImp listProductUseCaseImp){
        this.iListProductInput=iListProductInput;
        this.listProductUseCaseImp = listProductUseCaseImp;
    }

    @GetMapping("/getAllProducts")

    public ResponseEntity<List<ProductDTO>> GetAllProducts(){
        Collection<Product> list=iListProductInput.getProducts();
        List<ProductDTO> productDTOList=list.stream().map(ProductDTO::coreToDTO).toList();
        return ResponseEntity.status(HttpStatus.OK).body(productDTOList);
    }
    @GetMapping("/{product_id}")
    public ResponseEntity<Object> getProductById(@PathVariable("product_id") Long product_id){
       Optional<ProductEntity> productEntityOptional=this.listProductUseCaseImp.getProductId(product_id);
       if(productEntityOptional.isEmpty()){
           Map<String, String> response = new HashMap<>();
           response.put("message", "No encontrado");
           return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
       }
       return new ResponseEntity<>(productEntityOptional.get(),HttpStatus.OK);
    }
    @GetMapping("related/{category_id}/{product_id}")
    public ResponseEntity<Object> getRelatedProduct(@PathVariable("category_id") Long  category_id, @PathVariable("product_id") Long product_id){
        return new ResponseEntity<>(this.listProductUseCaseImp.getRelatedProducts(category_id,product_id),HttpStatus.OK);
    }
    @GetMapping("productsby/{category_id}")
    public ResponseEntity<?> getProductsByCategory(@PathVariable Long category_id){
        if (listProductUseCaseImp.getProductsByIdCategory(category_id).isEmpty()){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(listProductUseCaseImp.getProductsByIdCategory(category_id).stream().map(ProductDTO::coreToDTO).toList());
    }


}
