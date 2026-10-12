package com.ema.practica.infrastructure.moduloProduct.atributos.crud;

import com.ema.practica.infrastructure.moduloProduct.atributos.entity.VarianteAtributoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// VarianteAtributoRepository.java
@Repository
public interface VarianteAtributoRepository extends JpaRepository<VarianteAtributoEntity, Long> {
}