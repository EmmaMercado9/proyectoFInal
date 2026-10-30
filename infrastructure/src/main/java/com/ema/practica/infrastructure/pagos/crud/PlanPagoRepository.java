package com.ema.practica.infrastructure.pagos.crud;

import com.ema.practica.infrastructure.order.enumOrder.PaymentMethod;
import com.ema.practica.infrastructure.pagos.entity.PlanPagoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanPagoRepository extends JpaRepository<PlanPagoEntity, Long> {

    Optional<PlanPagoEntity> findByMedioAndCuotas(PaymentMethod medio, Integer cuotas);

    List<PlanPagoEntity> findAllByOrderByMedioAscCuotasAsc();
}
