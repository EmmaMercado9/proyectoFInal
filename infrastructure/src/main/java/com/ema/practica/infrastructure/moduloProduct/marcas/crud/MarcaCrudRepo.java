package com.ema.practica.infrastructure.moduloProduct.marcas.crud;

import com.ema.practica.infrastructure.moduloProduct.marcas.entity.MarcaEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface MarcaCrudRepo extends CrudRepository<MarcaEntity,Long> {

    boolean findByNombre(String nombre);

    MarcaEntity findByMarcaId(Long id);

    boolean existsByMarcaId(Long id);
}
