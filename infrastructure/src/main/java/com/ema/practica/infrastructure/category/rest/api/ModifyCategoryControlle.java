package com.ema.practica.infrastructure.category.rest.api;

import com.ema.practica.infrastructure.category.entity.CategoryProductEntity;
import com.ema.practica.infrastructure.category.rest.model.CategoryDTO;
import com.ema.practica.infrastructure.category.useCases.UpdateCategoryUseCaseImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/category/modify")
@CrossOrigin("*")

public class ModifyCategoryControlle {
    private final UpdateCategoryUseCaseImp updateCategoryUseCaseImp;
    @Autowired

    public ModifyCategoryControlle(UpdateCategoryUseCaseImp updateCategoryUseCaseImp){
        this.updateCategoryUseCaseImp=updateCategoryUseCaseImp;
    }

    @PutMapping
    public ResponseEntity<?> modifyUser(@RequestBody CategoryDTO categoryDTO) {
        CategoryProductEntity categoryProductEntity =categoryDTO.dtoToEntity();
        boolean result = updateCategoryUseCaseImp.updateCategory(
                categoryProductEntity.getNameCategory(),
                categoryProductEntity.getIdCategory()
        );
        return ResponseEntity.ok().body(result);
    }

}
