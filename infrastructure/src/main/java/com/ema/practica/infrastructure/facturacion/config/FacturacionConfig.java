package com.ema.practica.infrastructure.facturacion.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ArcaProperties.class)
public class FacturacionConfig {
}
