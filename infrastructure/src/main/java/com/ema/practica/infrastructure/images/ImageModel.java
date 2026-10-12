package com.ema.practica.infrastructure.images;

import com.ema.practica.infrastructure.moduloProduct.product.entity.ProductEntity;
import com.ema.practica.infrastructure.moduloProduct.productVariant.entity.ProductVariantEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name="image_model")
public class ImageModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "name")

    private String name;
    @Column(name = "url")

    private String url;
    @Column(name = "imageId")

    private String imageId;

    @Column(name = "es_principal", nullable = false)
    private Boolean esPrincipal = false;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = true) // Clave foránea en "image_model"
    private ProductEntity product;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = true)
    @JsonIgnore
    private ProductVariantEntity variant;

    public ImageModel(String originalFilename, String imageUrl, String imageId,Boolean esPrincipal) {
        this.name = originalFilename;
        this.url = imageUrl;
        this.imageId = imageId;
        this.esPrincipal=esPrincipal;

    }

    public ImageModel(Long id, String name, String url, String imageId) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.imageId = imageId;
    }

}
