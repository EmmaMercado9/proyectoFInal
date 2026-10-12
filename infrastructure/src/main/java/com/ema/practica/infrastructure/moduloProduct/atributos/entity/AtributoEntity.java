package com.ema.practica.infrastructure.moduloProduct.atributos.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "atributos")
@NoArgsConstructor
@Getter
@Setter
public class AtributoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "atributo_id")
    private Long atributoId;

    @Column(name = "nombre", nullable = false)
    private String nombre;  // "Talla", "Color", "Material"

    public AtributoEntity(Long atributoId, String nombre) {
        this.atributoId = atributoId;
        this.nombre     = nombre;
    }
}