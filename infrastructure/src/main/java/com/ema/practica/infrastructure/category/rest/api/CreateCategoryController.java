package com.ema.practica.infrastructure.category.rest.api;

import com.ema.practica.core.category.input.IAddCategoryInput;
import com.ema.practica.infrastructure.category.rest.model.CategoryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/AddCategory")
@CrossOrigin("*")

public class CreateCategoryController {

    IAddCategoryInput iAddCategoryInput;
    @Autowired
    public CreateCategoryController(IAddCategoryInput iAddCategoryInput ){
        this.iAddCategoryInput=iAddCategoryInput;
    }

    @PostMapping()
    public ResponseEntity<?> addProductCart(@RequestBody CategoryDTO categoryDTO){
        boolean result= iAddCategoryInput.addCategory(categoryDTO.dtoToCore());
        return  ResponseEntity.ok().body(result);
    }
}
