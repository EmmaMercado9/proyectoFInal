package com.ema.practica.infrastructure.moduloProduct.category.useCases;

import com.ema.practica.infrastructure.exception.RecursoNoEncontradoException;
import com.ema.practica.infrastructure.exception.VentaInvalidaException;
import com.ema.practica.infrastructure.moduloProduct.category.crud.CategoryRepository;
import com.ema.practica.infrastructure.moduloProduct.category.entity.CategoryProductEntity;
import com.ema.practica.infrastructure.moduloProduct.category.rest.mapper.CategoryMapper;
import com.ema.practica.infrastructure.moduloProduct.category.rest.model.CategoryRequestDTO;
import com.ema.practica.infrastructure.moduloProduct.category.rest.model.CategoryResponseDTO;
import com.ema.practica.infrastructure.moduloProduct.subcategoria.crud.SubCategoriaRepository;
import com.ema.practica.infrastructure.moduloProduct.subcategoria.entity.SubCategoriaEntity;
import com.ema.practica.infrastructure.moduloProduct.subcategoria.rest.model.SubCategoriaRequestDTO;
import com.ema.practica.infrastructure.moduloProduct.subcategoria.rest.model.SubCategoriaResponseDTO;
import com.ema.practica.infrastructure.util.status.StatusEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

// CategoryService.java
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final SubCategoriaRepository subCategoriaRepository;
    private final CategoryMapper categoryMapper;

    @Transactional
    public CategoryResponseDTO crearCategoria(CategoryRequestDTO dto) {
        if (categoryRepository.existsByNameCategory(dto.getNameCategory())) {
            throw new VentaInvalidaException("Ya existe una categoría con ese nombre");
        }
        CategoryProductEntity entity = new CategoryProductEntity(dto.getNameCategory());
        return categoryMapper.toResponseDTO(categoryRepository.save(entity));
    }

    @Transactional
    public SubCategoriaResponseDTO crearSubCategoria(SubCategoriaRequestDTO dto) {
        CategoryProductEntity categoria = buscarCategoria(dto.getCategoryId());

        SubCategoriaEntity sub = new SubCategoriaEntity(dto.getNombre(), categoria);
        SubCategoriaEntity guardada = subCategoriaRepository.save(sub);
        return categoryMapper.toSubDTO(guardada);
    }

    @Transactional
    public SubCategoriaResponseDTO moverSubCategoria(Long subId, Long nuevaCategoriaId) {
        SubCategoriaEntity sub = subCategoriaRepository.findById(subId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Subcategoría", subId));

        CategoryProductEntity nuevaCategoria = buscarCategoria(nuevaCategoriaId);

        sub.setCategoryProductEntity(nuevaCategoria);
        return categoryMapper.toSubDTO(sub);
    }

    @Transactional
    public CategoryResponseDTO renombrarCategoria(Long id, CategoryRequestDTO dto) {
        CategoryProductEntity categoria = buscarCategoria(id);
        String nuevoNombre = dto.getNameCategory();
        if (!categoria.getNameCategory().equalsIgnoreCase(nuevoNombre)
                && categoryRepository.existsByNameCategory(nuevoNombre)) {
            throw new VentaInvalidaException("Ya existe una categoría con ese nombre");
        }
        categoria.setNameCategory(nuevoNombre);
        return categoryMapper.toResponseDTO(categoria);
    }

    // Borra la categoría y sus subcategorías, solo si no tienen productos asociados
    @Transactional
    public void eliminarCategoria(Long id) {
        CategoryProductEntity categoria = buscarCategoria(id);
        if (categoria.getCantidadProductos() > 0) {
            throw new VentaInvalidaException(
                    "No se puede eliminar: la categoría tiene productos asociados. Desactivala o mové sus subcategorías.");
        }
        categoryRepository.delete(categoria);
    }

    private CategoryProductEntity buscarCategoria(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría", id));
    }

    public List<CategoryResponseDTO> listarTodas() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public void toggleStatusSub(Long subId) {
        SubCategoriaEntity sub = subCategoriaRepository.findById(subId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Subcategoría", subId));
        sub.setStatus(sub.getStatus() == StatusEntity.ACTIVO
                ? StatusEntity.INACTIVO : StatusEntity.ACTIVO);
    }
}