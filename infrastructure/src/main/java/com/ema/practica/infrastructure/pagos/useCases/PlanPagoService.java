package com.ema.practica.infrastructure.pagos.useCases;

import com.ema.practica.infrastructure.exception.VentaInvalidaException;
import com.ema.practica.infrastructure.order.enumOrder.PaymentMethod;
import com.ema.practica.infrastructure.pagos.crud.PlanPagoRepository;
import com.ema.practica.infrastructure.pagos.entity.PlanPagoEntity;
import com.ema.practica.infrastructure.pagos.rest.model.PlanPagoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Descuentos y recargos según el medio de pago y las cuotas.
 * El precio de los productos es el de lista; el efectivo tiene descuento
 * y cada plan de cuotas de la tarjeta de crédito puede tener su recargo.
 */
@Service
@RequiredArgsConstructor
public class PlanPagoService {

    /** Planes que se crean la primera vez (después se editan desde el panel). */
    static final List<PlanPagoEntity> POR_DEFECTO = List.of(
            new PlanPagoEntity(PaymentMethod.EFECTIVO, 1, "-10.00"),
            new PlanPagoEntity(PaymentMethod.TRANSFERENCIA, 1, "0.00"),
            new PlanPagoEntity(PaymentMethod.TARJETA_DEBITO, 1, "0.00"),
            new PlanPagoEntity(PaymentMethod.MERCADOPAGO, 1, "0.00"),
            new PlanPagoEntity(PaymentMethod.TARJETA_CREDITO, 1, "0.00"),
            new PlanPagoEntity(PaymentMethod.TARJETA_CREDITO, 3, "0.00"),
            new PlanPagoEntity(PaymentMethod.TARJETA_CREDITO, 4, "0.00"),
            new PlanPagoEntity(PaymentMethod.TARJETA_CREDITO, 6, "0.00"));

    private final PlanPagoRepository repository;

    @Transactional
    public List<PlanPagoDTO> listar() {
        asegurarPlanes();
        return repository.findAllByOrderByMedioAscCuotasAsc().stream().map(PlanPagoDTO::de).toList();
    }

    /** Crea los planes por defecto que falten. */
    void asegurarPlanes() {
        for (PlanPagoEntity p : POR_DEFECTO) {
            if (repository.findByMedioAndCuotas(p.getMedio(), p.getCuotas()).isEmpty()) {
                repository.save(new PlanPagoEntity(p.getMedio(), p.getCuotas(), p.getPorcentaje().toPlainString()));
            }
        }
    }

    @Transactional
    public List<PlanPagoDTO> guardar(List<PlanPagoDTO> planes) {
        for (PlanPagoDTO dto : planes) {
            validarCuotas(dto.medio(), dto.cuotas());
            PlanPagoEntity plan = repository.findByMedioAndCuotas(dto.medio(), dto.cuotas())
                    .orElseGet(() -> new PlanPagoEntity(dto.medio(), dto.cuotas(), "0"));
            plan.setPorcentaje(dto.porcentaje());
            plan.setActivo(dto.activo());
            repository.save(plan);
        }
        return listar();
    }

    /**
     * Porcentaje que corresponde a una venta: negativo = descuento, positivo = recargo.
     * Falla si el plan está desactivado o si se piden cuotas fuera de la tarjeta de crédito.
     */
    @Transactional(readOnly = true)
    public BigDecimal porcentajePara(PaymentMethod medio, Integer cuotas) {
        int c = cuotas != null ? cuotas : 1;
        validarCuotas(medio, c);
        return repository.findByMedioAndCuotas(medio, c)
                .map(p -> {
                    if (!p.isActivo()) {
                        throw new VentaInvalidaException(c > 1
                                ? "El pago en " + c + " cuotas no está habilitado."
                                : "El medio de pago " + medio + " no está habilitado.");
                    }
                    return p.getPorcentaje();
                })
                // Sin configurar todavía: el valor por defecto
                .orElseGet(() -> POR_DEFECTO.stream()
                        .filter(p -> p.getMedio() == medio && p.getCuotas() == c)
                        .map(PlanPagoEntity::getPorcentaje)
                        .findFirst()
                        .orElseThrow(() -> new VentaInvalidaException("El pago en " + c + " cuotas no está habilitado.")));
    }

    private static void validarCuotas(PaymentMethod medio, int cuotas) {
        if (medio != PaymentMethod.TARJETA_CREDITO && cuotas != 1) {
            throw new VentaInvalidaException("Solo la tarjeta de crédito se puede pagar en cuotas.");
        }
    }
}
