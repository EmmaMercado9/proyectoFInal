package com.ema.practica.infrastructure.importacion.lectores;

/**
 * Lector de facturas de un proveedor (patrón Strategy): cada formato de PDF tiene
 * su implementación y todas devuelven el mismo {@link FacturaLeida}.
 * Para sumar un proveedor nuevo alcanza con escribir otra clase con @Component.
 */
public interface LectorFactura {

    /** ¿Este texto es una factura del formato que sabe leer este lector? */
    boolean reconoce(String texto);

    /** Convierte el texto del PDF al formato común. */
    FacturaLeida leer(String texto);
}
