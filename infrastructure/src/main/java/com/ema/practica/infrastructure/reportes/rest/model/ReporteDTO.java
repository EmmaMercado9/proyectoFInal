package com.ema.practica.infrastructure.reportes.rest.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Reporte de gestión de un período. Cada sub-registro es un bloque de la pantalla.
 * Las ventas se cuentan por su fecha; las devoluciones se descuentan de la venta original.
 */
public record ReporteDTO(
        LocalDate desde,
        LocalDate hasta,
        Resumen actual,
        Resumen anterior,                 // mismo largo, inmediatamente antes (para comparar)
        List<Punto> ventasPorDia,
        List<Item> porMedioDePago,
        List<Item> porVendedor,
        List<Item> porCategoria,
        List<Item> porMarca,
        List<Item> porDiaDeSemana,
        List<Item> porHora,
        List<Producto> masVendidos,
        List<Producto> masRentables,
        Perdidas perdidas,
        Inventario inventario,
        Compras compras,
        Facturacion facturacion,
        Recomendaciones recomendaciones,
        List<String> observaciones,
        int detallesConCostoEstimado      // ventas viejas sin costo guardado: se usó el actual
) {

    /**
     * Indicadores principales. Ventas netas = brutas − descuentos + recargos − devoluciones.
     * Ganancia bruta = ventas netas − costo de lo vendido − pérdidas por fallados.
     */
    public record Resumen(BigDecimal ventasBrutas, BigDecimal descuentos, BigDecimal recargos, BigDecimal devoluciones,
                          BigDecimal ventasNetas, BigDecimal costoMercaderia, BigDecimal perdidaFallados,
                          BigDecimal gananciaBruta, Double margen, int cantidadVentas, int unidades,
                          BigDecimal ticketPromedio, int canceladas, BigDecimal montoCancelado) {
    }

    public record Punto(LocalDate fecha, BigDecimal ventas, BigDecimal ganancia, int cantidad) {
    }

    public record Item(String nombre, BigDecimal ventas, BigDecimal ganancia, int cantidad) {
    }

    public record Producto(String producto, String sku, int unidades, BigDecimal ventas,
                           BigDecimal ganancia, Double margen) {
    }

    public record Perdidas(BigDecimal perdidaFallados, int unidadesFalladas, BigDecimal perdidaVentasBajoCosto,
                           List<Producto> vendidosBajoCosto, BigDecimal descuentos, BigDecimal devoluciones,
                           int canceladas, BigDecimal montoCancelado) {
    }

    public record Inventario(int unidades, BigDecimal valorAlCosto, BigDecimal valorAlPrecio,
                             int variantesSinStock, int variantesBajoMinimo) {
    }

    public record Compras(int facturas, int unidades, BigDecimal total) {
    }

    public record Facturacion(String ambiente, int comprobantes, BigDecimal facturado,
                              BigDecimal notasCredito, BigDecimal neto, Double porcentajeDeVentas) {
    }

    /** Recomendaciones según las ventas de los últimos 30 y 90 días. */
    public record Recomendaciones(List<Reposicion> reponer, List<SinRotacion> sinRotacion,
                                  List<Producto> margenBajo, List<AltaDevolucion> altaDevolucion,
                                  List<TalleTop> tallesMasVendidos) {
    }

    public record Reposicion(String producto, String variante, String sku, int stock, int vendidas30,
                             Double diasCobertura, int sugerido, BigDecimal costoEstimado, boolean urgente) {
    }

    public record SinRotacion(String producto, String variante, String sku, int stock,
                              BigDecimal capitalInmovilizado) {
    }

    public record AltaDevolucion(String producto, int vendidas, int devueltas, double tasa) {
    }

    public record TalleTop(String producto, String talle, int unidades) {
    }
}
