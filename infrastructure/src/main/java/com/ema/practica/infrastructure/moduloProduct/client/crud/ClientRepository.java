package com.ema.practica.infrastructure.moduloProduct.client.crud;

import com.ema.practica.infrastructure.moduloProduct.client.entity.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<ClientEntity, UUID> {

    boolean existsByDni(String dni);
    @Query("""
        SELECT c FROM ClientEntity c
        WHERE
          LOWER(c.name)     LIKE LOWER(CONCAT('%',:q,'%')) OR
          c.dni             LIKE CONCAT('%',:q,'%')         OR
          LOWER(c.username) LIKE LOWER(CONCAT('%',:q,'%')) OR
          LOWER(c.mail)     LIKE LOWER(CONCAT('%',:q,'%'))
        ORDER BY c.name ASC
    """)
    List<ClientEntity> buscar(@Param("q") String q);
}