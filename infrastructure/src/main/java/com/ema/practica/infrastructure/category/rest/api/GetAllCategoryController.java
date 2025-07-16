package com.ema.practica.infrastructure.category.rest.api;
import com.ema.practica.infrastructure.category.entity.CategoryProductEntity;
import com.ema.practica.infrastructure.category.rest.model.CategoryDTO;
import com.ema.practica.infrastructure.category.useCases.ConsultCategoryUseCaseImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/getAllCatgory")
@CrossOrigin("*")

public class GetAllCategoryController {
    private final ConsultCategoryUseCaseImp consultCategoryUseCaseImp;
    @Autowired

    public GetAllCategoryController(ConsultCategoryUseCaseImp consultCategoryUseCaseImp){
        this.consultCategoryUseCaseImp=consultCategoryUseCaseImp;
    }

    @GetMapping()

    public ResponseEntity<List<CategoryDTO>> GetAllUsers(){
        Collection<CategoryProductEntity> list=consultCategoryUseCaseImp.getCategoryProducts();
        List<CategoryDTO> list1=list.stream().map(CategoryDTO::entityToDTO).toList();
        if (!list1.isEmpty()) {
            return ResponseEntity.ok(list1);
        } else {
            return ResponseEntity.noContent().build();
        }
    }
}
