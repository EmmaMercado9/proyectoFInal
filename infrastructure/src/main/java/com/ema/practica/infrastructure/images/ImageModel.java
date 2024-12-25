package com.ema.practica.infrastructure.images;

import com.ema.practica.core.images.ImageM;
import com.ema.practica.core.product.model.Product;
import com.ema.practica.infrastructure.category.entity.CategoryProductEntity;
import com.ema.practica.infrastructure.product.entity.ProductEntity;
import com.ema.practica.infrastructure.sizes.SizeEntity;
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
    private Long id;
    private String name;
    private String type;
    @Column(length = 50000000)
    private byte[] picByte;

    public ImageModel(String originalFilename, String contentType, byte[] bytes) {
        this.name = originalFilename;
        this.type = contentType;
        this.picByte = bytes;
    }

    public ImageM entityToCore() {
        return new ImageM(this.getId(),this.getName(),this.getType(),this.getPicByte());
    }

    public static ImageModel coreToEntity(ImageM imageM) {
        return new ImageModel(imageM.getId(),imageM.getName(),imageM.getType(),imageM.getPicByte());
    }

}
