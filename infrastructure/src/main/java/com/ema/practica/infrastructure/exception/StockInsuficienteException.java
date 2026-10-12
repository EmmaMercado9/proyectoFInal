package com.ema.practica.infrastructure.exception;

public class StockInsuficienteException extends RuntimeException {

    public StockInsuficienteException(Long variantId, Integer stock, Integer quantity) {
        super("Stock insuficiente para la variante " + variantId
                + ": disponible " + stock + ", solicitado " + quantity);
    }
}
