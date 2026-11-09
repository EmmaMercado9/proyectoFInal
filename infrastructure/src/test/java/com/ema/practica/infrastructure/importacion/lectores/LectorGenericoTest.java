package com.ema.practica.infrastructure.importacion.lectores;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class LectorGenericoTest {

    private final LectorGenerico lector = new LectorGenerico();

    static final String FACTURA_A_ARCA = """
            ORIGINAL
            Razón Social: DISTRIBUIDORA NORTE SRL
            Punto de Venta: 00003 Comp. Nro: 00001234
            Fecha de Emisión: 05/11/2026
            CUIT: 30712345678
            Código Producto / Servicio Cantidad U. Medida Precio Unit. % Bonif Subtotal Alícuota IVA Subtotal c/IVA
            RB100 Remera Básica Negra Talle M 2,00 unidades 1000,00 0,00 2000,00 21% 2420,00
            RB100 Remera Básica Negra Talle L 1,00 unidades 1000,00 10,00 900,00 21% 1089,00
            Importe Neto Gravado: $ 2900,00
            IVA 21%: $ 609,00
            Importe Total: $ 3509,00
            DUPLICADO
            RB100 Remera Básica Negra Talle M 2,00 unidades 1000,00 0,00 2000,00 21% 2420,00
            """;

    @Test
    void facturaDeArca_leeEncabezadoYRenglonesConIva_yNoRepiteLaCopia() {
        FacturaLeida f = lector.leer(FACTURA_A_ARCA);

        assertThat(f.proveedor()).isEqualTo("DISTRIBUIDORA NORTE SRL");
        assertThat(f.cuit()).isEqualTo("30-71234567-8");
        assertThat(f.numero()).isEqualTo("00003-00001234");
        assertThat(f.fecha()).isEqualTo(LocalDate.of(2026, 11, 5));
        assertThat(f.total()).isEqualByComparingTo("3509");
        assertThat(f.lineas()).hasSize(2);

        LineaFactura m = f.lineas().get(0);
        assertThat(m.codigoArticulo()).isEqualTo("RB100");
        assertThat(m.descripcion()).isEqualTo("Remera Básica Negra");
        assertThat(m.talle()).isEqualTo("M");
        assertThat(m.skuVariante()).isEqualTo("RB100-M");
        assertThat(m.cantidad()).isEqualTo(2);
        assertThat(m.costoUnitario()).isEqualByComparingTo("1210");     // con IVA
        assertThat(f.sumaLineas()).isEqualByComparingTo(f.total());
    }

    @Test
    void sinCodigoNiTalle_derivaElCodigoDeLaDescripcion() {
        FacturaLeida f = lector.leer("""
                Calzados del Sur
                C.U.I.T. 20-12345678-9
                Factura 0002-00000456
                Fecha: 01/11/2026
                Descripción Cant. Precio Importe
                Medias Algodón Pack 3 $ 1.500,00 $ 4.500,00
                TOTAL $ 4.500,00
                """);

        assertThat(f.proveedor()).isEqualTo("Calzados del Sur");
        assertThat(f.numero()).isEqualTo("0002-00000456");
        assertThat(f.lineas()).hasSize(1);
        LineaFactura l = f.lineas().get(0);
        assertThat(l.descripcion()).isEqualTo("Medias Algodón Pack");
        assertThat(l.codigoArticulo()).isEqualTo("MEDIAS-ALGODON-PACK");
        assertThat(l.talle()).isEqualTo("Único");
        assertThat(l.cantidad()).isEqualTo(3);
        assertThat(l.costoUnitario()).isEqualByComparingTo("1500");
    }

    @Test
    void talleNumericoAlFinal_yDescripcionEnElRenglonAnterior() {
        FacturaLeida f = lector.leer("""
                Zapatilla Urbana Blanca 40
                1 25.000,00 25.000,00
                TOTAL 25.000,00
                """);

        assertThat(f.lineas()).hasSize(1);
        assertThat(f.lineas().get(0).descripcion()).isEqualTo("Zapatilla Urbana Blanca");
        assertThat(f.lineas().get(0).talle()).isEqualTo("40");
    }

    @Test
    void renglonesNetos_siElTotalIncluyeIva_seLesSuma() {
        FacturaLeida f = lector.leer("""
                ART1 Buzo Gris 1 1.000,00 1.000,00
                Importe Total: $ 1.210,00
                """);

        assertThat(f.lineas().get(0).costoUnitario()).isEqualByComparingTo("1210");
    }

    @Test
    void ignoraTotalesYRenglonesQueNoCierran() {
        FacturaLeida f = lector.leer("""
                Subtotal 2 1.000,00 2.000,00
                Texto cualquiera 2 1.000,00 5.000,00
                Sin productos
                """);

        assertThat(f.lineas()).isEmpty();
    }
}
