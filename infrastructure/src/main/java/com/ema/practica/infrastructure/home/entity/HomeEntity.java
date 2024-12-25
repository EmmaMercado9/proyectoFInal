package com.ema.practica.infrastructure.home.entity;

import com.ema.practica.core.category.model.CategoryProduct;
import com.ema.practica.core.home.model.Home;
import com.ema.practica.core.product.model.Product;
import com.ema.practica.infrastructure.category.entity.CategoryProductEntity;
import com.ema.practica.infrastructure.product.entity.ProductEntity;
import com.ema.practica.infrastructure.user.entitty.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "homes")
public class HomeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idhome")
    private Long idHome;
    @Column(name = "street")
    private String street;
    @Column(name = "city")
    private String city;
    @Column(name = "location")
    private String Location;
    @Column(name = "nro")
    private int nro;
    @Column(name = "postcode")
    private int postcode;

    public Home entityToCore() {
        return Home.getInstance(this.getIdHome(),this.getStreet(),this.getCity(),this.getLocation(),this.getNro(),this.getPostcode());
    }

    public static HomeEntity coreToEntity(Home home) {
        return new HomeEntity(home.getIdHome(), home.getStreet(), home.getCity(), home.getLocation(), home.getNro(), home.getPostcode());
    }
}
