package com.ema.practica.infrastructure.moduloProduct.subcategoria.entity;

import com.ema.practica.infrastructure.moduloProduct.category.entity.CategoryProductEntity;
import com.ema.practica.infrastructure.moduloProduct.product.entity.ProductEntity;
import com.ema.practica.infrastructure.util.status.StatusEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

// SubCategoriaEntity.java
@Entity
@Setter
@Getter
@NoArgsConstructor
@Table(name = "sub_categorias")
public class SubCategoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sub_id")
    private Long subId;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusEntity status = StatusEntity.ACTIVO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_category", nullable = false)
    @JsonIgnore
    private CategoryProductEntity categoryProductEntity;

    @OneToMany(mappedBy = "subCategoria", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ProductEntity> productEntityList = new ArrayList<>();

    public SubCategoriaEntity(String nombre, CategoryProductEntity categoria) {
        this.nombre = nombre;
        this.categoryProductEntity = categoria;
        this.status = StatusEntity.ACTIVO;
    }
}