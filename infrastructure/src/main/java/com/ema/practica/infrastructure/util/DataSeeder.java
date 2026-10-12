package com.ema.practica.infrastructure.util;


import com.ema.practica.infrastructure.moduloProduct.atributos.crud.AtributoRepository;
import com.ema.practica.infrastructure.moduloProduct.atributos.entity.AtributoEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements ApplicationRunner {

    private final AtributoRepository atributoRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        List<String> atributosBase = List.of(
                "Talla", "Talle", "Color", "Material"
        );

        atributosBase.forEach(nombre -> {
            if (!atributoRepository.existsByNombreIgnoreCase(nombre)) {
                AtributoEntity atributo = new AtributoEntity();
                atributo.setNombre(nombre);
                atributoRepository.save(atributo);
                log.info("Atributo creado: {}", nombre);
            } else {
                log.info("Atributo ya existe, se omite: {}", nombre);
            }
        });
    }
}