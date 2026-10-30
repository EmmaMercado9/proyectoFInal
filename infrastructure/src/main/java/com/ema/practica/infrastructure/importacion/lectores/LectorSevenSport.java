package com.ema.practica.infrastructure.importacion.lectores;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Facturas de Factory Sport S.A. (Seven Sport). Cada artículo ocupa varios renglones:
 * <pre>
 * 1,00 KJ877033050 Zapatillas Mujer adidas Supernova Glide
 * KJ877033050 0,00 $ 93.499,00 $ 93.499,00
 * </pre>
 * cantidad · código · descripción (1 o 2 renglones) · código repetido · % bonif. · precio · subtotal.
 * El talle no figura: se deduce del código (artículo de 6 caracteres + 5 dígitos, los
 * 3 últimos son el talle US × 10). Es una deducción, por eso se confirma en la vista previa.
 */
@Component
public class LectorSevenSport implements LectorFactura {

    static final String CUIT = "33-71199188-9";

    /*
     * (\d+),\d{2}            cantidad ("1,00")
     * ([A-Z]{2}\d{4})(\d{5}) artículo + código de talle
     * (.+?)                  descripción, hasta que aparece el mismo código repetido (\2\3)
     * \$ precio \$ subtotal
     */
    private static final Pattern RENGLON = Pattern.compile(
            "(\\d+),\\d{2}\\s+([A-Z]{2}\\d{4})(\\d{5})\\s+(.+?)\\s*\\2\\3\\s+[\\d.,]+\\s+\\$\\s*([\\d.]+,\\d{2})\\s+\\$\\s*([\\d.]+,\\d{2})",
            Pattern.DOTALL);

    private static final Pattern TOTAL = Pattern.compile("(?m)^TOTAL\\s*\\$\\s*([\\d.]+,\\d{2})");

    private static final Map<String, String> MARCAS = Map.of(
            "adidas", "Adidas", "nike", "Nike", "puma", "Puma", "reebok", "Reebok", "topper", "Topper");

    @Override
    public boolean reconoce(String texto) {
        return texto.contains(CUIT);
    }

    @Override
    public FacturaLeida leer(String texto) {
        List<LineaFactura> lineas = new ArrayList<>();
        Matcher m = RENGLON.matcher(texto);
        while (m.find()) {
            int cantidad = Integer.parseInt(m.group(1));
            String articulo = m.group(2);
            String codigoTalle = m.group(3);
            String descripcion = m.group(4).replaceAll("\\s+", " ").trim();
            BigDecimal importe = Importes.ar(m.group(6));
            lineas.add(new LineaFactura(articulo, descripcion, marca(descripcion), talleUs(codigoTalle),
                    articulo + codigoTalle, cantidad, Importes.porUnidad(importe, cantidad),
                    null, importe));       // compra a un minorista: no hay precio de lista
        }
        String total = Importes.primero(TOTAL, texto);
        return new FacturaLeida("Factory Sport S.A. (Seven Sport)", CUIT, Importes.numero(texto),
                Importes.fecha(texto), total != null ? Importes.ar(total) : null, lineas);
    }

    /** "33050" → "US 5", "20085" → "US 8.5", "20100" → "US 10". */
    static String talleUs(String codigoTalle) {
        int decimas = Integer.parseInt(codigoTalle.substring(2));
        BigDecimal talle = BigDecimal.valueOf(decimas, 1).stripTrailingZeros();
        return "US " + talle.toPlainString();
    }

    private static String marca(String descripcion) {
        String d = descripcion.toLowerCase();
        return MARCAS.entrySet().stream().filter(e -> d.contains(e.getKey()))
                .map(Map.Entry::getValue).findFirst().orElse("");
    }
}
