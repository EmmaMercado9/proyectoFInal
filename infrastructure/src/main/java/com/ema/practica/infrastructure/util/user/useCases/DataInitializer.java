package com.ema.practica.infrastructure.util.user.useCases;

import com.ema.practica.infrastructure.util.rol.Rol;
import com.ema.practica.infrastructure.util.status.StatusEntity;
import com.ema.practica.infrastructure.util.user.crud.ICreateUserCrud;
import com.ema.practica.infrastructure.util.user.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private ICreateUserCrud createUserCrud;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (createUserCrud.findByName("admin").isEmpty()) {

            UserEntity admin = new UserEntity();
            admin.setName("admin");
            admin.setMail("admin@gmail.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRol(Rol.ADMIN);
            admin.setStatus(StatusEntity.ACTIVO);
            createUserCrud.save(admin);
        }
    }
}