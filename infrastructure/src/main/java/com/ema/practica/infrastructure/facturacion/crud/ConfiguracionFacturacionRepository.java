package com.ema.practica.infrastructure.facturacion.crud;

import com.ema.practica.infrastructure.facturacion.entity.ConfiguracionFacturacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfiguracionFacturacionRepository extends JpaRepository<ConfiguracionFacturacionEntity, Long> {
}
