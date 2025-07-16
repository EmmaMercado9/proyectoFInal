package com.ema.practica.infrastructure.category.entity;


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


}
