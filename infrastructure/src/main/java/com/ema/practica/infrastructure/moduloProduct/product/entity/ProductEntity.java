package com.ema.practica.infrastructure.moduloProduct.product.entity;

import com.ema.practica.infrastructure.images.ImageModel;
import com.ema.practica.infrastructure.moduloProduct.atributos.entity.AtributoEntity;
import com.ema.practica.infrastructure.moduloProduct.productVariant.entity.ProductVariantEntity;
import com.ema.practica.infrastructure.moduloProduct.subcategoria.entity.SubCategoriaEntity;
import com.ema.practica.infrastructure.util.status.StatusEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Id;
import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.util.*;
// ProductEntity.java — el único cambio: quitar stock/stockMinimo y
// cambiar @JoinColumn por mappedBy en la relación con variantes
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "products")
public class ProductEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "product_id", updatable = false, nullable = false)
    private UUID idProduct;

    @Column(name = "codigo_barra", unique = true)
    private String codigoBarra;

    @Column(name = "sku", unique = true)
    private String sku;

    @Column(name = "name_product", nullable = false)
    private String nameProduct;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "marca")
    private String marca;

    @Column(name = "modelo")
    private String modelo;

    @Column(name = "precio_venta", precision = 10, scale = 2)
    private BigDecimal precioVenta;

    @Column(name = "precio_pro", precision = 10, scale = 2)
    private BigDecimal precioPro;

    @Column(name = "costo", precision = 10, scale = 2)
    private BigDecimal costo;

    @Column(name = "margen")
    private Integer margen;

    // ELIMINADO stock y stockMinimo — viven en la variante

    @Column(name = "tiene_variantes", nullable = false)
    private Boolean tieneVariantes = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusEntity status = StatusEntity.ACTIVO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_id")
    private SubCategoriaEntity subCategoria;

    // Corregido: mappedBy en lugar de @JoinColumn
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ProductVariantEntity> variants = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<ImageModel> imageModelSet = new HashSet<>();

    // Métodos de negocio útiles
    public void agregarVariante(ProductVariantEntity variante) {
        variante.setProduct(this);
        this.variants.add(variante);
    }

    // Las variantes desactivadas no se venden: no suman stock ni alertan
    public Integer getStockTotal() {
        return variants.stream()
                .filter(v -> v.getStatus() != StatusEntity.INACTIVO)
                .mapToInt(v -> v.getStock() != null ? v.getStock() : 0)
                .sum();
    }

    public boolean tieneStockBajo() {
        return variants.stream()
                .filter(v -> v.getStatus() != StatusEntity.INACTIVO)
                .anyMatch(v -> v.getStock() != null &&
                        v.getStockMinimo() != null &&
                        v.getStock() <= v.getStockMinimo());
    }


}