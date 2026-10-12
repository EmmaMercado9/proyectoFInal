package com.ema.practica.infrastructure.order.enumOrder;

public enum OrderStatus {
    PENDIENTE,
    COMPLETADA,
    CANCELADA,
    EN_ENVIO,         // para canal ONLINE
    DEVUELTA_PARCIAL, // se devolvió parte de los productos
    DEVUELTA          // se devolvieron todos los productos
}