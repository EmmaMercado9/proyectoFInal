package com.ema.practica.infrastructure.category.rest.api;
import com.ema.practica.infrastructure.category.rest.model.CategoryDTO;
import com.ema.practica.infrastructure.category.useCases.AddCategoryUseCaseImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/AddCategory")
@CrossOrigin("*")

public class CreateCategoryController {


    private final AddCategoryUseCaseImp addCategoryUseCaseImp;
    @Autowired
    public CreateCategoryController( AddCategoryUseCaseImp addCategoryUseCaseImp ){
        this.addCategoryUseCaseImp=addCategoryUseCaseImp;
    }

    @PostMapping()
    public ResponseEntity<?> addProductCart(@RequestBody CategoryDTO categoryDTO){
        boolean result= addCategoryUseCaseImp.save(categoryDTO.dtoToEntity());
        return  ResponseEntity.ok().body(result);
    }
}
