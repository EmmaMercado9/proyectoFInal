package com.ema.practica.infrastructure.importacion.lectores;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Utilidades comunes a los lectores. */
public final class Importes {

    private static final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Pattern FECHA = Pattern.compile("Fecha:\\s*(\\d{2}/\\d{2}/\\d{4})");
    // "N°: 0258 - 08518823" o "Nº00058-00236125"
    private static final Pattern NUMERO = Pattern.compile("N[°º]\\s*:?\\s*(\\d{4,5})\\s*-\\s*(\\d{8})");

    private Importes() {
    }

    /** "1.234.567,89" → 1234567.89 (formato argentino: punto de miles, coma decimal). */
    public static BigDecimal ar(String s) {
        return new BigDecimal(s.trim().replace(".", "").replace(",", "."));
    }

    public static BigDecimal porUnidad(BigDecimal importe, int cantidad) {
        return importe.divide(BigDecimal.valueOf(cantidad), 2, RoundingMode.HALF_UP);
    }

    public static LocalDate fecha(String texto) {
        Matcher m = FECHA.matcher(texto);
        return m.find() ? LocalDate.parse(m.group(1), DMY) : null;
    }

    public static String numero(String texto) {
        Matcher m = NUMERO.matcher(texto);
        return m.find() ? m.group(1) + "-" + m.group(2) : null;
    }

    /** Primer grupo de la primera coincidencia, o null. */
    public static String primero(Pattern p, String texto) {
        Matcher m = p.matcher(texto);
        return m.find() ? m.group(1) : null;
    }
}
