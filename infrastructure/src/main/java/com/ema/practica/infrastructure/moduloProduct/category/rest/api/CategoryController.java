package com.ema.practica.infrastructure.moduloProduct.category.rest.api;


import com.ema.practica.infrastructure.exception.VentaInvalidaException;
import com.ema.practica.infrastructure.moduloProduct.category.rest.model.CategoryRequestDTO;
import com.ema.practica.infrastructure.moduloProduct.category.rest.model.CategoryResponseDTO;
import com.ema.practica.infrastructure.moduloProduct.category.useCases.CategoryService;
import com.ema.practica.infrastructure.moduloProduct.subcategoria.rest.model.SubCategoriaRequestDTO;
import com.ema.practica.infrastructure.moduloProduct.subcategoria.rest.model.SubCategoriaResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryResponseDTO> crear(@RequestBody @Valid CategoryRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.crearCategoria(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> renombrar(
            @PathVariable Long id, @RequestBody @Valid CategoryRequestDTO dto) {
        return ResponseEntity.ok(categoryService.renombrarCategoria(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        categoryService.eliminarCategoria(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/subcategorias")
    public ResponseEntity<SubCategoriaResponseDTO> crearSub(
            @RequestBody @Valid SubCategoriaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.crearSubCategoria(dto));
    }

    @PatchMapping("/subcategorias/{subId}/mover")
    public ResponseEntity<SubCategoriaResponseDTO> mover(
            @PathVariable Long subId, @RequestParam Long categoryId) {
        return ResponseEntity.ok(categoryService.moverSubCategoria(subId, categoryId));
    }

    @PatchMapping("/subcategorias/{subId}/toggle-status")
    public ResponseEntity<Void> toggleStatus(@PathVariable Long subId) {
        categoryService.toggleStatusSub(subId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> listar() {
        return ResponseEntity.ok(categoryService.listarTodas());
    }
}