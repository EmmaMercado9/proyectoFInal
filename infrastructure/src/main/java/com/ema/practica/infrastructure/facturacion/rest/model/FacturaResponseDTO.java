package com.ema.practica.infrastructure.facturacion.rest.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class FacturaResponseDTO {
    private Long idFactura;
    private String tipo;                 // FACTURA_C | NOTA_CREDITO_C
    private String tipoDescripcion;      // "Factura C"
    private int codigoTipo;              // 11 | 13
    private String letra = "C";
    private Integer puntoVenta;
    private Long numero;
    private String numeroFormateado;     // 00001-00000023
    private LocalDate fecha;
    private BigDecimal importeTotal;
    private String cae;
    private LocalDate caeVencimiento;
    private String ambiente;
    private boolean prueba;              // homologación: sin validez fiscal
    private String qrUrl;

    // Receptor
    private Integer docTipo;
    private Long docNro;
    private Integer condicionIvaReceptorId;
    private String condicionIvaReceptor;
    private String receptorNombre;

    // Venta y relaciones
    private Long idOrder;
    private UUID trackingIdOrden;
    private Long idFacturaAsociada;
    private String facturaAsociadaNumero;
    private Long idDevolucion;
    private String motivo;
    private String usuario;
    private LocalDateTime creadoEn;

    // Para la nota de crédito: cuánto queda por anular de esta factura
    private BigDecimal saldoAnulable;

    private Emisor emisor;
    private List<Item> items;

    @Data
    public static class Emisor {
        private Long cuit;
        private String razonSocial;
        private String domicilio;
        private String iibb;
        private String inicioActividades;
        private String condicionIva;
    }

    @Data
    public static class Item {
        private String descripcion;
        private BigDecimal cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;

        public Item(String descripcion, BigDecimal cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {
            this.descripcion = descripcion;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
            this.subtotal = subtotal;
        }
    }
}
