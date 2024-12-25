package com.ema.practica.infrastructure.category.rest.api;

import com.ema.practica.core.category.input.IUpdateCategoryInput;
import com.ema.practica.core.category.model.CategoryProduct;

import com.ema.practica.infrastructure.category.rest.model.CategoryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/category/modify")
@CrossOrigin("*")

public class ModifyCategoryControlle {
    IUpdateCategoryInput iUpdateCategoryInput;

    @Autowired

    public ModifyCategoryControlle(IUpdateCategoryInput iUpdateCategoryInput){
        this.iUpdateCategoryInput=iUpdateCategoryInput;
    }

    @PutMapping
    public ResponseEntity<?> modifyUser(@RequestBody CategoryDTO categoryDTO) {
        CategoryProduct categoryProduct =categoryDTO.dtoToCore();
        boolean result = iUpdateCategoryInput.updateCategory(
                categoryProduct.getNameCategory(),
                categoryProduct.getIdCategory()
        );
        return ResponseEntity.ok().body(result);
    }

}
