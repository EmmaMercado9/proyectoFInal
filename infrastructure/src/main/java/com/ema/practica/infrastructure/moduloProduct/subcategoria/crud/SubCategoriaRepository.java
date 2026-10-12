package com.ema.practica.infrastructure.moduloProduct.subcategoria.crud;

import com.ema.practica.infrastructure.moduloProduct.subcategoria.entity.SubCategoriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubCategoriaRepository extends JpaRepository<SubCategoriaEntity, Long> {

    List<SubCategoriaEntity> findByCategoryProductEntity_IdCategory(Long categoryId);

    SubCategoriaEntity findBySubId(Long id);
    @Query("SELECT s FROM SubCategoriaEntity s WHERE s.status = 'ACTIVO'")
    List<SubCategoriaEntity> findAllActivas();
}