package com.ema.practica.infrastructure.moduloProduct.category.entity;


import com.ema.practica.infrastructure.moduloProduct.subcategoria.entity.SubCategoriaEntity;
import com.ema.practica.infrastructure.util.status.StatusEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

// CategoryProductEntity.java
@Entity
@Setter
@Getter
@NoArgsConstructor
@Table(name = "categorys")
public class CategoryProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_category")
    private Long idCategory;

    @Column(name = "name_category", nullable = false, unique = true)
    private String nameCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusEntity status = StatusEntity.ACTIVO;

    @OneToMany(mappedBy = "categoryProductEntity", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SubCategoriaEntity> subCategorias = new ArrayList<>();

    public CategoryProductEntity(String nameCategory) {
        this.nameCategory = nameCategory;
        this.status = StatusEntity.ACTIVO;
    }

    // Útil para el árbol: cuántos productos tiene esta categoría en total
    public Integer getCantidadProductos() {
        return subCategorias.stream()
                .mapToInt(s -> s.getProductEntityList().size())
                .sum();
    }
}