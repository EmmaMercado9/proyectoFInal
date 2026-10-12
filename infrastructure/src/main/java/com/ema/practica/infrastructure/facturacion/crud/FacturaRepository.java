package com.ema.practica.infrastructure.facturacion.crud;

import com.ema.practica.infrastructure.facturacion.entity.FacturaEntity;
import com.ema.practica.infrastructure.facturacion.entity.TipoComprobante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface FacturaRepository extends JpaRepository<FacturaEntity, Long> {

    List<FacturaEntity> findByOrder_IdOrderOrderByCreadoEnAsc(Long idOrder);

    // Por ambiente: una factura de prueba no impide facturar de verdad después
    boolean existsByOrder_IdOrderAndTipoAndAmbiente(Long idOrder, TipoComprobante tipo, String ambiente);

    boolean existsByDevolucion_IdDevolucion(Long idDevolucion);

    // Cuánto de una factura ya se anuló con notas de crédito
    @Query("SELECT COALESCE(SUM(f.importeTotal), 0) FROM FacturaEntity f WHERE f.facturaAsociada.idFactura = :idFactura")
    BigDecimal sumNotasCredito(@Param("idFactura") Long idFactura);
}
