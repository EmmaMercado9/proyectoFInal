package com.ema.practica.infrastructure.moduloProduct.productVariant.crud;

import com.ema.practica.infrastructure.moduloProduct.productVariant.entity.ProductVariantEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// ProductVariantRepository.java
@Repository
public interface ProductVariantRepository
        extends JpaRepository<ProductVariantEntity, Long> {
    @Query("""
    SELECT v FROM ProductVariantEntity v
    WHERE v.codigoBarra = :codigo
    AND v.status = 'ACTIVO'
    AND v.esDefault = false
""")
    Optional<ProductVariantEntity> findByCodigoBarra(
            @Param("codigo") String codigo
    );

    boolean existsBySkuAndVariantIdNot(String sku, Long variantId);

    boolean existsByCodigoBarraAndVariantIdNot(String codigoBarra, Long variantId);

    // FOR UPDATE bloquea la fila hasta que termine la transacción
    // Evita que dos ventas simultáneas vendan el mismo stock
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM ProductVariantEntity v WHERE v.variantId = :id")
    Optional<ProductVariantEntity> findByIdForUpdate(@Param("id") Long id);
}