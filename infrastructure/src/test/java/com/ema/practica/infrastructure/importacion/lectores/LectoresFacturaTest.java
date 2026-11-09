package com.ema.practica.infrastructure.importacion.lectores;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Textos con el mismo formato que devuelve PDFBox para cada factura
 * (sin los datos personales del cliente).
 */
class LectoresFacturaTest {

    static final String ADIDAS = """
            C.U.I.T: 30-68514022-1
            Fecha vencimiento: 10/10/2026
            Fecha: 10/10/2026
            N°: 0258 - 08518823
            Transporte: 1.749.985,30
            IH2098 Botines PREDATOR CLUB Lengüeta Plegable Terreno Firme/Multiterreno\s
            Niños36.5 (UK 5)
            UNIDAD 1 99.999,000 30,00 69.999,30
            ID8797 Zapatillas adidas VL Court 3.036.5 (UK 5) UNIDAD 1 119.999,000 30,00 83.999,30
            IH9536 Zapatillas Runfalcon 638.5 (UK 6.5) UNIDAD 2 119.999,000 30,00 167.998,60
            IH9536 Zapatillas Runfalcon 639 (UK 7) UNIDAD 1 119.999,000 30,00 83.999,30
            SON PESOS : Cuatrocientos Cinco Mil Novecientos Noventa Y Seis Con 50/100.
            405.996,50TOTAL PESOS
            """;

    static final String SEVEN = """
            Subtotal Gravado $ 335.536,36
            TOTAL $ 271.147,00
            CONCEPTOS
            Cantidad Código Descripción % Bonificación Precio Unitario Subtotal
            1,00 KJ877033050 Zapatillas Mujer adidas Supernova Glide
            KJ877033050 0,00 $ 93.499,00 $ 93.499,00
            1,00 ID876020085 Zapatillas Hombre adidas Galaxy 7
            ID876020085 0,00 $ 84.149,00 $ 84.149,00
            1,00 KJ875810070 Zapatillas Hombre adidas Supernova
            Glide KJ875810070 0,00 $ 93.499,00 $ 93.499,00
            FACTURA B Nº00058-00236125
            Fecha: 21/07/2026
            CUIT: 33-71199188-9
            """;

    private final LectorAdidas adidas = new LectorAdidas();
    private final LectorSevenSport seven = new LectorSevenSport();

    @Test
    void cadaLectorReconoceSoloSuFactura() {
        assertThat(adidas.reconoce(ADIDAS)).isTrue();
        assertThat(adidas.reconoce(SEVEN)).isFalse();
        assertThat(seven.reconoce(SEVEN)).isTrue();
        assertThat(seven.reconoce(ADIDAS)).isFalse();
    }

    @Test
    void adidas_leeEncabezadoRenglonesYTotal() {
        FacturaLeida f = adidas.leer(ADIDAS);

        assertThat(f.numero()).isEqualTo("0258-08518823");
        assertThat(f.fecha()).isEqualTo(LocalDate.of(2026, 10, 10));
        assertThat(f.lineas()).hasSize(4);
        assertThat(f.total()).isEqualByComparingTo("405996.50");
        assertThat(f.sumaLineas()).isEqualByComparingTo(f.total());
    }

    @Test
    void adidas_separaElTallePegadoAlNombre() {
        LineaFactura runfalcon = adidas.leer(ADIDAS).lineas().get(2);

        assertThat(runfalcon.codigoArticulo()).isEqualTo("IH9536");
        assertThat(runfalcon.descripcion()).isEqualTo("Zapatillas Runfalcon 6");   // el "6" es del modelo
        assertThat(runfalcon.talle()).isEqualTo("38.5");
        assertThat(runfalcon.skuVariante()).isEqualTo("IH9536-38.5");
        assertThat(runfalcon.cantidad()).isEqualTo(2);
        assertThat(runfalcon.costoUnitario()).isEqualByComparingTo("83999.30");  // importe / cantidad
        assertThat(runfalcon.precioLista()).isEqualByComparingTo("119999");
    }

    @Test
    void adidas_juntaLaDescripcionCortadaEnDosRenglones() {
        LineaFactura predator = adidas.leer(ADIDAS).lineas().get(0);

        assertThat(predator.descripcion()).endsWith("Terreno Firme/Multiterreno Niños");
        assertThat(predator.talle()).isEqualTo("36.5");
        assertThat(predator.importe()).isEqualByComparingTo("69999.30");
    }

    @Test
    void seven_leeRenglonesDeVariasLineasYDeduceElTalle() {
        FacturaLeida f = seven.leer(SEVEN);

        assertThat(f.numero()).isEqualTo("00058-00236125");
        assertThat(f.lineas()).hasSize(3);
        assertThat(f.sumaLineas()).isEqualByComparingTo(f.total());

        LineaFactura glide = f.lineas().get(2);
        assertThat(glide.codigoArticulo()).isEqualTo("KJ8758");
        assertThat(glide.descripcion()).isEqualTo("Zapatillas Hombre adidas Supernova Glide");
        assertThat(glide.skuVariante()).isEqualTo("KJ875810070");
        assertThat(glide.talle()).isEqualTo("US 7");
        assertThat(glide.marca()).isEqualTo("Adidas");
        assertThat(glide.precioLista()).isNull();
    }

    @Test
    void seven_leeRopaConTalleEnLetras() {
        FacturaLeida f = seven.leer("""
                TOTAL $ 112.453,00
                1,00 KJ895142090 Zapatillas Hombre adidas Terrex Eastrail
                3 KJ895142090 0,00 $ 80.324,00 $ 80.324,00
                1,00 KD393260L Remera Hombre adidas Cookie
                KD393260L 0,00 $ 21.419,00 $ 21.419,00
                1,00 JZ116733XL Remera Hombre adidas Terrex Classic
                JZ116733XL 0,00 $ 10.710,00 $ 10.710,00
                """);

        assertThat(f.lineas()).hasSize(3);
        assertThat(f.lineas().get(0).talle()).isEqualTo("US 9");
        assertThat(f.lineas().get(1).codigoArticulo()).isEqualTo("KD3932");
        assertThat(f.lineas().get(1).talle()).isEqualTo("L");
        assertThat(f.lineas().get(2).talle()).isEqualTo("XL");
        assertThat(f.sumaLineas()).isEqualByComparingTo(f.total());
    }

    @Test
    void seven_talleUs() {
        assertThat(LectorSevenSport.talleUs("33050")).isEqualTo("US 5");
        assertThat(LectorSevenSport.talleUs("20085")).isEqualTo("US 8.5");
        assertThat(LectorSevenSport.talleUs("20100")).isEqualTo("US 10");
    }

    @Test
    void importesArgentinos() {
        assertThat(Importes.ar("2.869.976,20")).isEqualByComparingTo(new BigDecimal("2869976.20"));
        assertThat(Importes.ar("99.999,000")).isEqualByComparingTo("99999");
    }
}
