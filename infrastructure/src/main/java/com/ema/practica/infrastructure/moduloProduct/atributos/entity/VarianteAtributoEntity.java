package com.ema.practica.infrastructure.moduloProduct.atributos.entity;

import com.ema.practica.infrastructure.moduloProduct.productVariant.entity.ProductVariantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// VarianteAtributoEntity.java — reemplaza el @ManyToMany
// Esta es la tabla puente que tenías pero le faltaba el valor
@Entity
@Table(name = "variante_atributos",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"variant_id", "atributo_id"} // una variante no repite atributo
        )
)
@Getter
@Setter
@NoArgsConstructor
public class VarianteAtributoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "variante_atributo_id")
    private Long varianteAtributoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariantEntity variante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atributo_id", nullable = false)
    private AtributoEntity atributo;  // "Talla", "Color"

    @Column(name = "valor", nullable = false)
    private String valor;             // "42", "Rojo"

    public VarianteAtributoEntity(ProductVariantEntity variante,
                                  AtributoEntity atributo,
                                  String valor) {
        this.variante = variante;
        this.atributo = atributo;
        this.valor    = valor;
    }
}