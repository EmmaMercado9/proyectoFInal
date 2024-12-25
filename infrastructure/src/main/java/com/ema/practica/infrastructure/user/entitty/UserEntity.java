package com.ema.practica.infrastructure.user.entitty;
import com.ema.practica.core.user.model.User;
import com.ema.practica.infrastructure.home.entity.HomeEntity;
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
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "idhome", referencedColumnName = "idhome")
    private HomeEntity home;
    @Column(name = "role")
    private String role;
    @Column(name = "name")
    private String name;
    @Column(name = "mail")
    private String mail;
    @Column(name = "password")
    private String password;

    public User entityToCore() {
            return User.instance(this.getIdUser(),getHome().entityToCore(), this.getName(),this.getMail(),this.getPassword());
    }

    public static UserEntity coreToEntity(User user) {
        return new UserEntity(user.getIdUser(), HomeEntity.coreToEntity(user.getHome()), user.getRole(), user.getName(), user.getMail(), user.getPassword());
    }
}
