package com.ema.practica.infrastructure.moduloProduct.atributos.crud;

import com.ema.practica.infrastructure.moduloProduct.atributos.entity.AtributoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface AtributoRepository extends JpaRepository<AtributoEntity, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

}