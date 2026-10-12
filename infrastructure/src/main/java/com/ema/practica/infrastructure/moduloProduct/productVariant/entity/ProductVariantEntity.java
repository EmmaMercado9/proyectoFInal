package com.ema.practica.infrastructure.moduloProduct.productVariant.entity;

import com.ema.practica.infrastructure.images.ImageModel;
import com.ema.practica.infrastructure.moduloProduct.atributos.entity.AtributoEntity;
import com.ema.practica.infrastructure.moduloProduct.atributos.entity.VarianteAtributoEntity;
import com.ema.practica.infrastructure.moduloProduct.product.entity.ProductEntity;
import com.ema.practica.infrastructure.util.status.StatusEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// ProductVariantEntity.java — corregida completa
@Entity
@Table(name = "product_variants")
@Getter
@Setter
@NoArgsConstructor
public class ProductVariantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "variant_id")
    private Long variantId;

    // Relación al producto padre — bidireccional
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private ProductEntity product;

    @Column(name = "sku", unique = true)
    private String sku;

    @Column(name = "codigo_barra", unique = true)
    private String codigoBarra;      // camelCase

    @Column(name = "precio_venta", precision = 10, scale = 2)
    private BigDecimal precioVenta;  // camelCase

    @Column(name = "precio_pro", precision = 10, scale = 2)
    private BigDecimal precioPro;    // camelCase

    @Column(name = "costo", precision = 10, scale = 2)
    private BigDecimal costo;

    @Column(name = "margen")
    private Integer margen;

    @Column(name = "stock", nullable = false)
    private Integer stock = 0;

    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo = 5; // camelCase

    @Column(name = "es_default", nullable = false)
    private Boolean esDefault = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private StatusEntity status = StatusEntity.ACTIVO;

    @OneToMany(mappedBy = "variant",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<ImageModel> imageModelSet = new HashSet<>();


    // Reemplaza el @ManyToMany — ahora tiene el campo valor
    @OneToMany(mappedBy = "variante", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    private List<VarianteAtributoEntity> atributos = new ArrayList<>();

    // Métodos de negocio
    public String getValorAtributo(String nombreAtributo) {
        return atributos.stream()
                .filter(a -> a.getAtributo().getNombre()
                        .equalsIgnoreCase(nombreAtributo))
                .map(VarianteAtributoEntity::getValor)
                .findFirst()
                .orElse(null);
    }

    // Precio efectivo: el propio o cae al del producto
    public BigDecimal getPrecioEfectivo() {
        if (this.precioVenta != null) return this.precioVenta;
        return this.product.getPrecioVenta();
    }

    public boolean tieneStockSuficiente(int cantidad) {
        return this.stock != null && this.stock >= cantidad;
    }

    // Descripción legible: "Talla 42 / Color Rojo"
    public String getDescripcionAtributos() {
        return atributos.stream()
                .map(a -> a.getAtributo().getNombre() + " " + a.getValor())
                .collect(Collectors.joining(" / "));
    }
}