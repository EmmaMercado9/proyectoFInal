package com.ema.practica.infrastructure.moduloProduct.product.crud;
import com.ema.practica.infrastructure.moduloProduct.product.entity.ProductEntity;
import com.ema.practica.infrastructure.util.status.StatusEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

// ProductRepository.java
@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {

    Optional<ProductEntity> findByCodigoBarra(String codigoBarra);
    Optional<ProductEntity> findBySku(String sku);

    @Query("""
    SELECT p FROM ProductEntity p
    WHERE
      (:nombre IS NULL OR
       LOWER(p.nameProduct) LIKE LOWER(CONCAT('%',:nombre,'%')) OR
       LOWER(p.marca)       LIKE LOWER(CONCAT('%',:nombre,'%')) OR
       LOWER(p.sku)         LIKE LOWER(CONCAT('%',:nombre,'%')))
    AND
      (:status IS NULL OR p.status = :status)
    ORDER BY p.nameProduct ASC
""")
    Page<ProductEntity> listarConFiltros(
            @Param("nombre") String nombre,
            @Param("status") StatusEntity status,
            Pageable pageable
    );

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdProductNot(String sku, UUID idProduct);

    boolean existsByCodigoBarraAndIdProductNot(String codigoBarra, UUID idProduct);

    @Query("SELECT p FROM ProductEntity p WHERE " +
            "(:nombre IS NULL OR LOWER(p.nameProduct) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
            "(:status IS NULL OR p.status = :status)")
    Page<ProductEntity> buscar(
            @Param("nombre") String nombre,
            @Param("status") StatusEntity status,
            Pageable pageable
    );
}
