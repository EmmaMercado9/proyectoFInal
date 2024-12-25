package com.ema.practica.infrastructure.category.rest.api;

import com.ema.practica.core.cart.input.IConsultCartInput;
import com.ema.practica.core.cart.model.Cart;
import com.ema.practica.core.category.input.IConsultCategoryInput;
import com.ema.practica.core.category.model.CategoryProduct;
import com.ema.practica.infrastructure.category.rest.model.CategoryDTO;
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
    IConsultCategoryInput iConsultCategoryInput;
    @Autowired

    public GetAllCategoryController(IConsultCategoryInput iConsultCategoryInput){
        this.iConsultCategoryInput=iConsultCategoryInput;
    }

    @GetMapping()

    public ResponseEntity<List<CategoryDTO>> GetAllUsers(){
        Collection<CategoryProduct> list=iConsultCategoryInput.getCategoryProducts();
        List<CategoryDTO> list1=list.stream().map(CategoryDTO::coreToDTO).toList();
        if (!list1.isEmpty()) {
            return ResponseEntity.ok(list1);
        } else {
            return ResponseEntity.noContent().build();
        }
    }
}
