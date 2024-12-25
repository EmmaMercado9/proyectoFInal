package com.ema.practica.infrastructure.sizes;

import com.ema.practica.core.product.model.Product;
import com.ema.practica.core.sizes.Size;
import com.ema.practica.infrastructure.category.entity.CategoryProductEntity;
import com.ema.practica.infrastructure.product.entity.ProductEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "sizes")
public class SizeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idsize;

    private String size; // Ej: "38 (UK 6)"
    private int stock;
    private boolean available;




    public static Size entityToCore(SizeEntity size) {
        return new Size(size.getIdsize(),size.getSize(),size.getStock(),size.isAvailable());
    }
    public static SizeEntity coreToEntity(Size size) {
        return new SizeEntity(size.getId(),size.getSize(),size.getStock(),size.isAvailable());
    }

}
