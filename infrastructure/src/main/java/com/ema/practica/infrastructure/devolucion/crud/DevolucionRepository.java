package com.ema.practica.infrastructure.devolucion.crud;

import com.ema.practica.infrastructure.devolucion.entity.DevolucionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface DevolucionRepository extends JpaRepository<DevolucionEntity, Long> {

    List<DevolucionEntity> findByOrder_IdOrderOrderByFechaDesc(Long idOrder);

    Page<DevolucionEntity> findAllByOrderByFechaDesc(Pageable pageable);

    // Total ya reintegrado de una venta (para cerrar centavos en la última devolución)
    @Query("SELECT COALESCE(SUM(d.montoDevuelto), 0) FROM DevolucionEntity d WHERE d.order.idOrder = :idOrder")
    BigDecimal sumMontoDevueltoByOrder(@Param("idOrder") Long idOrder);
}
