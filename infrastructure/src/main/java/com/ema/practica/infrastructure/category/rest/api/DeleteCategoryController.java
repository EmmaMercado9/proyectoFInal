package com.ema.practica.infrastructure.category.rest.api;
import com.ema.practica.infrastructure.category.useCases.DeleteCategoryUseCaseImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/deleteCategory")
@CrossOrigin("*")

public class DeleteCategoryController {

    private final DeleteCategoryUseCaseImp deleteCategoryUseCaseImp;
    @Autowired
    public DeleteCategoryController(DeleteCategoryUseCaseImp deleteCategoryUseCaseImp){
        this.deleteCategoryUseCaseImp=deleteCategoryUseCaseImp;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id){

        boolean resultado=deleteCategoryUseCaseImp.deleteCategory(id);
        if (resultado) {
            return ResponseEntity.ok().body("Categoría eliminada correctamente");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Categoría no encontrada");
        }
    }
}
