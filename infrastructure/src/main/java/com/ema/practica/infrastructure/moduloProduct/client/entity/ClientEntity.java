package com.ema.practica.infrastructure.moduloProduct.client.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
public class ClientEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "idClient", updatable = false, nullable = false)
    private UUID idClient;

    @Column(name = "name")
    private String name;

    @Column(name = "username")
    private String username;

    @Column(name = "mail")
    private String mail;


    @Column(name = "dni")
    private String dni;

}
