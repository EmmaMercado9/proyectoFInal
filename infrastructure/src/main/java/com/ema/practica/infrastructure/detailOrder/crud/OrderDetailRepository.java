package com.ema.practica.infrastructure.detailOrder.crud;

import com.ema.practica.infrastructure.detailOrder.entity.OrderDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetailEntity, Long> {

    // Para no borrar productos/variantes que ya figuran en ventas
    boolean existsByVariant_VariantId(Long variantId);

    boolean existsByVariant_Product_IdProduct(java.util.UUID productId);
}