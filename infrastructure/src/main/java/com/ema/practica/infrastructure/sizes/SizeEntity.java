package com.ema.practica.infrastructure.sizes;


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






}
