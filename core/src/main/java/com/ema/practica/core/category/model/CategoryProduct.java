package com.ema.practica.core.category.model;

import com.ema.practica.core.product.model.Product;

import java.util.ArrayList;
import java.util.List;

public class CategoryProduct {
    private  Long idCategory;
    private String nameCategory;


    private  CategoryProduct(Long idCategory, String nameCategory) {
        this.idCategory = idCategory;
        this.nameCategory = nameCategory;
    }
    public  static CategoryProduct Instance(Long idCategory, String nameCategory) {
        return new CategoryProduct(idCategory, nameCategory);
    }

    public Long getIdCategory() {
        return idCategory;
    }

    public void setIdCategory(Long idCategory) {
        this.idCategory = idCategory;
    }

    public String getNameCategory() {
        return nameCategory;
    }

    public void setNameCategory(String nameCategory) {
        this.nameCategory = nameCategory;
    }

}
