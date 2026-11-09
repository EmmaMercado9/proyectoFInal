package com.ema.practica.infrastructure.order.crud;

import com.ema.practica.infrastructure.order.entity.OrderEntity;
import com.ema.practica.infrastructure.order.enumOrder.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    Optional<OrderEntity> findByTrackingId(UUID trackingId);

    Page<OrderEntity> findByUser_IdUser(UUID userId, Pageable pageable);

    @Query("""
            SELECT COUNT(o) FROM OrderEntity o
            WHERE o.status IN :estados AND o.orderDate >= :desde
            AND NOT EXISTS (SELECT 1 FROM FacturaEntity f WHERE f.order = o AND f.ambiente = :ambiente
                            AND f.tipo = com.ema.practica.infrastructure.facturacion.entity.TipoComprobante.FACTURA_C)
            """)
    long contarSinFacturar(
            @Param("estados") Collection<OrderStatus> estados,
            @Param("desde") LocalDateTime desde,
            @Param("ambiente") String ambiente
    );

    // Para reportes de ventas por día
    @Query("SELECT o FROM OrderEntity o WHERE o.orderDate BETWEEN :desde AND :hasta")
    List<OrderEntity> findByFechaBetween(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta
    );
}