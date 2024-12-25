package com.ema.practica.infrastructure.product.entity;

import com.ema.practica.core.product.model.Product;
import com.ema.practica.infrastructure.category.entity.CategoryProductEntity;
import com.ema.practica.infrastructure.images.ImageModel;
import com.ema.practica.infrastructure.sizes.SizeEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Id;
import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.engine.jdbc.Size;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "products")
public class ProductEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "idproduct")
    private  Long idProduct;
    @ManyToOne(fetch = FetchType.LAZY ,optional = false)
    @JoinColumn(name = "idcategory",nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private CategoryProductEntity categoryProduct;
    @Column(name = "nameproduct")
    private String nameProduct;
    @Column(name = "priceunitary")
    private double priceUnitary;
    @Column(name = "discontuedprice")
    private double discontuedPrice;
    @Column(name = "descriptionproduct")
    private String descriptionProduct;
    @Column(name = "status")
    private boolean status;
    @Column(name = "color")
    private String color;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "idsize")
    @JsonIgnore
    private List<SizeEntity> sizes = new ArrayList<>();

    @ManyToMany(fetch=FetchType.EAGER ,cascade = CascadeType.ALL)
    @JoinTable(name="product_images",
        joinColumns={
            @JoinColumn(name="product_id")
        },inverseJoinColumns = {
            @JoinColumn(name = "image_id")

    }
    )
    private Set<ImageModel> imageModelSet;


    public Product entityToCore() {
        return Product.getInstance(this.getIdProduct(),this.getCategoryProduct().entityToCore(),this.getNameProduct(),this.getPriceUnitary(),this.getDiscontuedPrice(),this.getDescriptionProduct(),this.isStatus(),this.getColor(),this.getSizes().stream().map(SizeEntity::entityToCore).toList(),this.getImageModelSet().stream().map(ImageModel::entityToCore).collect(Collectors.toSet()));
    }

    public static ProductEntity coreToEntity(Product product) {
        return new ProductEntity(product.getIdProduct(),CategoryProductEntity.coreToEntity(product.getCategory()),product.getNameProduct(),product.getPriceUnitary(),product.getDiscontuedPrice(),product.getDescriptionProduct(),product.isStatus(),product.getColor(),product.getSizes().stream().map(SizeEntity::coreToEntity).toList(),product.getImageMSet().stream().map(ImageModel::coreToEntity).collect(Collectors.toSet()));
    }

}
