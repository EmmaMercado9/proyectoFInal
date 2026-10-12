package com.ema.practica.infrastructure.util.user.entity;
import com.ema.practica.infrastructure.audit.AuditableEntity;
import com.ema.practica.infrastructure.util.rol.Rol;
import com.ema.practica.infrastructure.util.status.StatusEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class UserEntity extends AuditableEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "idUser", updatable = false, nullable = false)
    private UUID idUser;
    @Column(name = "rol")
    @Enumerated(EnumType.STRING)
    private Rol rol;
    @Column(name = "name")
    private String name;
    @Column(name = "mail")
    private String mail;
    @Column(name = "password")
    private String password;
    @Column(name="status")
    @Enumerated(EnumType.STRING)
    private StatusEntity status;
}
