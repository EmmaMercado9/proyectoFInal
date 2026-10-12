package com.ema.practica.infrastructure.moduloProduct.marcas.entity;

import com.ema.practica.infrastructure.util.status.StatusEntity;
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
@Table(name = "marcas")
public class MarcaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "marca_id")
    private Long marcaId;
    @Column(name = "nombre")
    private String nombre;
    @Column(name = "status")
    private StatusEntity status;
}
