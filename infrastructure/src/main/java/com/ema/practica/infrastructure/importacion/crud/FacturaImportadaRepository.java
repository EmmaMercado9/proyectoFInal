package com.ema.practica.infrastructure.importacion.crud;

import com.ema.practica.infrastructure.importacion.entity.FacturaImportadaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacturaImportadaRepository extends JpaRepository<FacturaImportadaEntity, Long> {

    Optional<FacturaImportadaEntity> findByCuitAndNumero(String cuit, String numero);

    List<FacturaImportadaEntity> findAllByOrderByImportadaEnDesc();

    List<FacturaImportadaEntity> findByFechaBetween(java.time.LocalDate desde, java.time.LocalDate hasta);
}
