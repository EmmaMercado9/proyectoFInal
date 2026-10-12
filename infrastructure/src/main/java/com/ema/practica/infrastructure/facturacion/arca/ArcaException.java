package com.ema.practica.infrastructure.facturacion.arca;

/** Error de comunicación o rechazo de ARCA. Se informa al usuario como 502/400. */
public class ArcaException extends RuntimeException {
    public ArcaException(String message) {
        super(message);
    }

    public ArcaException(String message, Throwable cause) {
        super(message, cause);
    }
}
