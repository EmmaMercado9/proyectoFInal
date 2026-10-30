package com.ema.practica.infrastructure.importacion.lectores;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Facturas de Adidas Argentina S.A. Cada artículo es un renglón:
 * <pre>IH9536 Zapatillas Runfalcon 638.5 (UK 6.5) UNIDAD 2 119.999,000 30,00 167.998,60</pre>
 * código · descripción con el talle EU pegado · (talle UK) · cantidad · precio de lista · % dto · importe.
 * Algunas descripciones largas se cortan en dos renglones.
 */
@Component
public class LectorAdidas implements LectorFactura {

    static final String CUIT = "30-68514022-1";

    /*
     * ^([A-Z]{2}\d{4})               código del artículo, al inicio del renglón
     * ((?:(?!^[A-Z]{2}\d{4}\s).)+?)  descripción (perezosa, puede ocupar 2 renglones,
     *                                pero nunca "se come" el renglón del artículo siguiente)
     * (\d{2}(?:\.5)?)\s*\(UK         talle EU: 2 dígitos + ".5" opcional, justo antes de "(UK"
     * UNIDAD cantidad precio %dto importe
     */
    private static final Pattern RENGLON = Pattern.compile(
            "^([A-Z]{2}\\d{4})\\s+((?:(?!^[A-Z]{2}\\d{4}\\s).)+?)\\s*(\\d{2}(?:\\.5)?)\\s*\\(UK\\s*[\\d.]+\\)\\s*"
                    + "(?:UNIDAD|PAR)\\s+(\\d+)\\s+([\\d.]+,\\d+)\\s+([\\d.]+,\\d+)\\s+([\\d.]+,\\d+)",
            Pattern.MULTILINE | Pattern.DOTALL);

    // El total puede venir antes o después de la etiqueta, según cómo se extraiga el texto
    private static final Pattern TOTAL_ANTES = Pattern.compile("([\\d.]+,\\d{2})\\s*TOTAL PESOS");
    private static final Pattern TOTAL_DESPUES = Pattern.compile("TOTAL PESOS\\s*([\\d.]+,\\d{2})");

    @Override
    public boolean reconoce(String texto) {
        return texto.contains(CUIT);
    }

    @Override
    public FacturaLeida leer(String texto) {
        List<LineaFactura> lineas = new ArrayList<>();
        Matcher m = RENGLON.matcher(texto);
        while (m.find()) {
            String codigo = m.group(1);
            String descripcion = m.group(2).replaceAll("\\s+", " ").trim();
            String talle = m.group(3);
            int cantidad = Integer.parseInt(m.group(4));
            BigDecimal precioLista = Importes.ar(m.group(5));
            BigDecimal importe = Importes.ar(m.group(7));
            lineas.add(new LineaFactura(codigo, descripcion, "Adidas", talle, codigo + "-" + talle,
                    cantidad, Importes.porUnidad(importe, cantidad), precioLista, importe));
        }
        String total = Importes.primero(TOTAL_ANTES, texto);
        if (total == null) {
            total = Importes.primero(TOTAL_DESPUES, texto);
        }
        return new FacturaLeida("Adidas Argentina S.A.", CUIT, Importes.numero(texto), Importes.fecha(texto),
                total != null ? Importes.ar(total) : null, lineas);
    }
}
