package com.ema.practica.infrastructure.util.user.useCases;

import com.ema.practica.infrastructure.util.rol.Rol;
import com.ema.practica.infrastructure.util.status.StatusEntity;
import com.ema.practica.infrastructure.util.user.crud.ICreateUserCrud;
import com.ema.practica.infrastructure.util.user.entity.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private ICreateUserCrud createUserCrud;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Value("${app.admin.email:admin@gmail.com}")
    private String adminEmail;

    @Override
    public void run(String... args) {

        if (createUserCrud.findByName("admin").isEmpty()) {

            // Sin contraseña configurada no se crea: nunca hay una clave conocida en el código
            if (adminPassword == null || adminPassword.isBlank()) {
                log.warn("No se creó el usuario admin: falta configurar ADMIN_PASSWORD");
                return;
            }

            UserEntity admin = new UserEntity();
            admin.setName("admin");
            admin.setMail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRol(Rol.ADMIN);
            admin.setStatus(StatusEntity.ACTIVO);
            createUserCrud.save(admin);
        }
    }
}