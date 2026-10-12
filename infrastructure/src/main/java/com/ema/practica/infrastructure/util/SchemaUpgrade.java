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
    }
}
