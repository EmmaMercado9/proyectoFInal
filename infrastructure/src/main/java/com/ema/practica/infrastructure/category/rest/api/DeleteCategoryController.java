package com.ema.practica.infrastructure.category.rest.api;

import com.ema.practica.core.cart.input.IDeleteProductCartInput;
import com.ema.practica.core.cart.model.Cart;
import com.ema.practica.core.category.input.IDeleteCategoryInput;
import com.ema.practica.core.category.model.CategoryProduct;
import com.ema.practica.infrastructure.category.rest.model.CategoryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/deleteCategory")
@CrossOrigin("*")

public class DeleteCategoryController {

    IDeleteCategoryInput iDeleteCategoryInput;
    @Autowired

    public DeleteCategoryController(IDeleteCategoryInput iDeleteCategoryInput){
        this.iDeleteCategoryInput=iDeleteCategoryInput;
    }

    @DeleteMapping()
    public ResponseEntity<?> loginUser(@RequestBody CategoryDTO categoryDTO){
        CategoryProduct categoryProduct=categoryDTO.dtoToCore();
        boolean resultado=iDeleteCategoryInput.deleteCategory(categoryProduct);
        return ResponseEntity.ok().body(resultado);
    }
}
