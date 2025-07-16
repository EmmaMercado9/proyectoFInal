package com.ema.practica.infrastructure.user.entitty;
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
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iduser")
    private Long idUser;
    @Column(name = "role")
    private String role;
    @Column(name = "name")
    private String name;
    @Column(name = "mail")
    private String mail;
    @Column(name = "password")
    private String password;

}
