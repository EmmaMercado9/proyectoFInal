package com.ema.practica.infrastructure.category.entity;

import com.ema.practica.core.category.model.CategoryProduct;
import com.ema.practica.core.product.model.Product;
import com.ema.practica.infrastructure.product.entity.ProductEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "categorys")

public class CategoryProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idcategory")

    private  Long idCategory;
    @Column(name = "namecategory")
    private String nameCategory;

    public CategoryProduct entityToCore() {
        return CategoryProduct.Instance(this.getIdCategory(), this.getNameCategory());
    }

    public static CategoryProductEntity coreToEntity(CategoryProduct categoryProduct) {
        return new CategoryProductEntity(categoryProduct.getIdCategory(),categoryProduct.getNameCategory());
    }
}
