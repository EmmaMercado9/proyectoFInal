package com.ema.practica.infrastructure.images;

import com.ema.practica.infrastructure.product.entity.ProductEntity;
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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false) // Clave foránea en "image_model"
    private ProductEntity product;

    public ImageModel(String originalFilename, String imageUrl, String imageId) {
        this.name = originalFilename;
        this.url = imageUrl;
        this.imageId = imageId;
    }

    public ImageModel(Long id, String name, String url, String imageId) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.imageId = imageId;
    }

}
