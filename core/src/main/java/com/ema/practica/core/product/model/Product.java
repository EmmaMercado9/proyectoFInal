package com.ema.practica.core.product.model;

import com.ema.practica.core.category.model.CategoryProduct;
import com.ema.practica.core.images.ImageM;
import com.ema.practica.core.sizes.Size;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Product {

    private  Long idProduct;
    private CategoryProduct category;
    private String nameProduct;
    private double priceUnitary;
    private double discontuedPrice;

    private String descriptionProduct;
    private boolean status;
    private String color;
    private List<Size> sizes = new ArrayList<>();
    private Set<ImageM> imageMSet;

    public Product(){}

    private Product(Long idProduct, CategoryProduct category, String nameProduct, double priceUnitary,double discontuedPrice, String descriptionProduct, boolean status, String color,List<Size> sizes,Set<ImageM> imageMSet){
        this.idProduct = idProduct;
        this.category=category;
        this.nameProduct = nameProduct;
        this.priceUnitary = priceUnitary;
        this.discontuedPrice=discontuedPrice;
        this.descriptionProduct = descriptionProduct;
        this.status = status;
        this.color = color;
        this.sizes=sizes;
        this.imageMSet=imageMSet;
    }

    public double getDiscontuedPrice() {
        return discontuedPrice;
    }

    public void setDiscontuedPrice(double discontuedPrice) {
        this.discontuedPrice = discontuedPrice;
    }

    public static Product getInstance(Long idProduct, CategoryProduct category, String nameProduct, double priceUnitary,double discontuedPrice, String descriptionProduct, boolean status, String color,List<Size> sizes,Set<ImageM> imageMSet){
        return new Product(idProduct,category, nameProduct, priceUnitary,discontuedPrice, descriptionProduct, status, color,sizes,imageMSet);
    }

    public List<Size> getSizes() {
        return sizes;
    }

    public void setSizes(List<Size> sizes) {
        this.sizes = sizes;
    }

    public Long getIdProduct() {
        return idProduct;
    }

    public void setIdProduct(Long idProduct) {
        this.idProduct = idProduct;
    }

    public CategoryProduct getCategory() {
        return category;
    }

    public void setCategory(CategoryProduct category) {
        this.category = category;
    }

    public String getNameProduct() {
        return nameProduct;
    }

    public void setNameProduct(String nameProduct) {
        this.nameProduct = nameProduct;
    }

    public double getPriceUnitary() {
        return priceUnitary;
    }

    public void setPriceUnitary(double priceUnitary) {
        this.priceUnitary = priceUnitary;
    }

    public String getDescriptionProduct() {
        return descriptionProduct;
    }

    public void setDescriptionProduct(String descriptionProduct) {
        this.descriptionProduct = descriptionProduct;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }


    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Set<ImageM> getImageMSet() {
        return imageMSet;
    }

    public void setImageMSet(Set<ImageM> imageMSet) {
        this.imageMSet = imageMSet;
    }
}
