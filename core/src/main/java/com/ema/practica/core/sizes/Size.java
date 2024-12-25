package com.ema.practica.core.sizes;

import com.ema.practica.core.product.model.Product;

public class Size {

    private Long id;

    private String size; // Ej: "38 (UK 6)"

    private int stock;

    private boolean available;
    public Size(){

    }

    public Size(Long id, String size, int stock, boolean available) {
        this.id = id;
        this.size = size;
        this.stock = stock;
        this.available = available;
    }

    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
