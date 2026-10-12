package com.ema.practica.infrastructure.exception;

// Regla de negocio violada (SKU duplicado, descuento inválido, etc.) → 400
public class VentaInvalidaException extends RuntimeException {

    public VentaInvalidaException(String message) {
        super(message);
    }
}
