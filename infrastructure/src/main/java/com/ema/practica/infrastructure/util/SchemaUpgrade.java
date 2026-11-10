package com.ema.practica.infrastructure.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Ajustes de esquema que ddl-auto=update no hace solo.
 *
 * orders.status se creó como ENUM de MySQL; los estados nuevos
 * (DEVUELTA_PARCIAL, DEVUELTA) fallarían al guardarse. Se pasa a VARCHAR
 * una única vez (si ya es VARCHAR no hace nada).
 *
 * product_variants.sku era UNIQUE: los talles de un mismo artículo comparten el
 * SKU de la etiqueta, así que se quita esa restricción (el servicio controla que
 * no se repita entre productos distintos).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SchemaUpgrade implements ApplicationRunner {

    private final JdbcTemplate jdbc;

    @Override
    public void run(ApplicationArguments args) {
        try {
            List<String> tipo = jdbc.queryForList("""
                    SELECT DATA_TYPE FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'status'
                    """, String.class);
            if (!tipo.isEmpty() && "enum".equalsIgnoreCase(tipo.get(0))) {
                jdbc.execute("ALTER TABLE orders MODIFY COLUMN status VARCHAR(30) NOT NULL");
                log.info("orders.status migrado de ENUM a VARCHAR(30)");
            }
        } catch (Exception e) {
            log.warn("No se pudo verificar/migrar orders.status: {}", e.getMessage());
        }
        try {
            List<String> indices = jdbc.queryForList("""
                    SELECT DISTINCT INDEX_NAME FROM information_schema.STATISTICS
                    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'product_variants'
                      AND COLUMN_NAME = 'sku' AND NON_UNIQUE = 0
                    """, String.class);
            for (String indice : indices) {
                jdbc.execute("ALTER TABLE product_variants DROP INDEX `" + indice.replace("`", "") + "`");
                log.info("product_variants.sku: se quitó el índice único {}", indice);
            }
        } catch (Exception e) {
            log.warn("No se pudo verificar/migrar product_variants.sku: {}", e.getMessage());
        }
    }
}
