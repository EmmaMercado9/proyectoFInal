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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// ProductRepository.java
@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {

    Optional<ProductEntity> findByCodigoBarra(String codigoBarra);
    Optional<ProductEntity> findBySku(String sku);

    @Query("""
    SELECT p FROM ProductEntity p LEFT JOIN p.subCategoria s
    WHERE
      (:nombre IS NULL OR
       LOWER(p.nameProduct) LIKE LOWER(CONCAT('%',:nombre,'%')) OR
       LOWER(p.marca)       LIKE LOWER(CONCAT('%',:nombre,'%')) OR
       LOWER(p.sku)         LIKE LOWER(CONCAT('%',:nombre,'%')))
    AND
      (:status IS NULL OR p.status = :status)
    AND
      (:marca IS NULL OR LOWER(p.marca) = LOWER(:marca))
    AND
      (:conVariantes IS NULL OR p.tieneVariantes = :conVariantes)
    AND
      (:categoriaId IS NULL OR s.categoryProductEntity.idCategory = :categoriaId)
    AND
      (:talle IS NULL OR EXISTS (
         SELECT 1 FROM ProductVariantEntity v JOIN v.atributos a
         WHERE v.product = p AND v.status <> com.ema.practica.infrastructure.util.status.StatusEntity.INACTIVO
           AND LOWER(a.atributo.nombre) LIKE 'tall%' AND a.valor = :talle))
    AND
      (:color IS NULL OR EXISTS (
         SELECT 1 FROM ProductVariantEntity v JOIN v.atributos a
         WHERE v.product = p AND v.status <> com.ema.practica.infrastructure.util.status.StatusEntity.INACTIVO
           AND LOWER(a.atributo.nombre) LIKE 'color%' AND a.valor = :color))
    AND
      (:stock IS NULL
       OR (:stock = 'SIN' AND NOT EXISTS (SELECT 1 FROM ProductVariantEntity v
             WHERE v.product = p AND v.status <> com.ema.practica.infrastructure.util.status.StatusEntity.INACTIVO AND v.stock > 0))
       OR (:stock = 'CON' AND EXISTS (SELECT 1 FROM ProductVariantEntity v
             WHERE v.product = p AND v.status <> com.ema.practica.infrastructure.util.status.StatusEntity.INACTIVO AND v.stock > 0))
       OR (:stock = 'BAJO' AND EXISTS (SELECT 1 FROM ProductVariantEntity v
             WHERE v.product = p AND v.status <> com.ema.practica.infrastructure.util.status.StatusEntity.INACTIVO AND v.stock > 0)
           AND EXISTS (SELECT 1 FROM ProductVariantEntity v
             WHERE v.product = p AND v.status <> com.ema.practica.infrastructure.util.status.StatusEntity.INACTIVO AND v.stock <= v.stockMinimo)))
    ORDER BY p.nameProduct ASC
""")
    Page<ProductEntity> listarConFiltros(
            @Param("nombre") String nombre,
            @Param("status") StatusEntity status,
            @Param("marca") String marca,
            @Param("categoriaId") Long categoriaId,
            @Param("talle") String talle,
            @Param("color") String color,
            @Param("stock") String stock,
            @Param("conVariantes") Boolean conVariantes,
            Pageable pageable
    );

    @Query("SELECT DISTINCT p.marca FROM ProductEntity p WHERE p.marca IS NOT NULL AND p.marca <> ''")
    List<String> marcas();

    @Query("""
    SELECT DISTINCT a.valor FROM VarianteAtributoEntity a
    WHERE LOWER(a.atributo.nombre) LIKE :prefijo AND a.variante.status <> com.ema.practica.infrastructure.util.status.StatusEntity.INACTIVO
    """)
    List<String> valoresDeAtributo(@Param("prefijo") String prefijo);

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
