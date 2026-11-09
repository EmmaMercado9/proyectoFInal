package com.ema.practica.infrastructure.importacion.lectores;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lector para facturas de cualquier proveedor: se usa cuando ningún lector específico
 * reconoce el PDF. No depende de columnas fijas: un renglón es de producto si entre sus
 * números hay cantidad × precio = importe. Lo que lee se revisa en la vista previa.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class LectorGenerico implements LectorFactura {

    static final String SIN_TALLE = "Único";

    private static final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final List<BigDecimal> ALICUOTAS = List.of(
            new BigDecimal("1.21"), new BigDecimal("1.105"), new BigDecimal("1.27"));

    private static final Pattern CUIT = Pattern.compile(
            "C\\.?\\s?U\\.?\\s?I\\.?\\s?T\\.?[^0-9]{0,15}(\\d{2})-?(\\d{8})-?(\\d)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern RAZON_SOCIAL = Pattern.compile(
            "Raz[oó]n\\s+Social\\s*:\\s*(.+?)(?:\\s+(?:Fecha|Punto|Comp|CUIT|Domicilio)\\b.*)?$",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    private static final Pattern NUMERO_GUION = Pattern.compile("(?<![\\d-])(\\d{4,5})\\s*-\\s*(\\d{8})(?![\\d-])");
    private static final Pattern NUMERO_AFIP = Pattern.compile(
            "Punto\\s+de\\s+Venta\\s*:?\\s*(\\d{1,5})\\s+Comp\\.?\\s*Nro\\s*:?\\s*(\\d{1,8})", Pattern.CASE_INSENSITIVE);
    private static final Pattern FECHA_EMISION = Pattern.compile(
            "Fecha(?:\\s+de\\s+Emisi[oó]n)?\\s*:?\\s*(\\d{2}[/-]\\d{2}[/-]\\d{4})", Pattern.CASE_INSENSITIVE);
    private static final Pattern TOTAL = Pattern.compile(
            "(?<![a-záéíóú])total\\b[^0-9\\n]*([\\d.]+,\\d{2})", Pattern.CASE_INSENSITIVE);
    private static final Pattern FECHA_O_CUIT = Pattern.compile(
            "\\b\\d{2}[/-]\\d{2}[/-]\\d{2,4}\\b|\\b\\d{2}-\\d{8}-\\d\\b");

    private static final Pattern TOKEN_NUMERO = Pattern.compile(
            "\\$?-?(?:\\d{1,3}(?:\\.\\d{3})+(?:,\\d+)?|\\d+(?:,\\d+)?|\\d+\\.\\d{1,2})%?");
    private static final Pattern NO_ES_PRODUCTO = Pattern.compile(
            "\\b(sub\\s*total|total|iva|neto|gravado|percepci[oó]n|retenci[oó]n|tributos|transporte"
                    + "|saldo|cae|vencimiento|cuit|fecha|domicilio|tel[eé]fono)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern ENCABEZADO_TABLA = Pattern.compile(
            "\\b(cantidad|cant\\.?|precio|descripci[oó]n|c[oó]digo|detalle|importe)\\b", Pattern.CASE_INSENSITIVE);
    private static final Set<String> UNIDADES_DE_MEDIDA = Set.of(
            "un", "uni", "unid", "unidad", "unidades", "par", "pares", "c/u", "$", "x");

    private static final Pattern TALLE_ROTULADO = Pattern.compile(
            "[\\s\\-/(]+(?:talle|talla|tal\\.?)\\s*[:.\\-]?\\s*(XXXL|XXL|XL|XS|S|M|L|\\d{1,2}(?:[.,]5)?)\\b\\)?",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern TALLE_AL_FINAL = Pattern.compile(
            "[\\s\\-/]+(XXXL|XXL|XL|XS|S|M|L|3[3-9](?:[.,]5)?|4\\d(?:[.,]5)?|50)$");
    // "Plegable40 (UK 8)": el talle pegado al nombre y su equivalencia entre paréntesis
    private static final Pattern TALLE_CON_EQUIVALENCIA = Pattern.compile(
            "(3[3-9](?:[.,]5)?|4\\d(?:[.,]5)?|50)?\\s*\\((?:UK|US|BR|EU)\\s*[\\d.,]+\\)$", Pattern.CASE_INSENSITIVE);

    @Override
    public boolean reconoce(String texto) {
        return true;
    }

    @Override
    public boolean esGenerico() {
        return true;
    }

    @Override
    public FacturaLeida leer(String texto) {
        String factura = soloPrimeraCopia(texto);
        BigDecimal total = total(factura);
        return new FacturaLeida(proveedor(factura), cuit(factura), numero(factura), fecha(factura), total,
                ajustarIva(renglones(factura), total));
    }

    /** Las facturas de ARCA repiten todo como ORIGINAL, DUPLICADO y TRIPLICADO. */
    private static String soloPrimeraCopia(String texto) {
        Matcher m = Pattern.compile("(?m)^\\s*DUPLICADO\\s*$").matcher(texto);
        return m.find() ? texto.substring(0, m.start()) : texto;
    }

    // ─── Encabezado ───────────────────────────────────────────────────────────

    private static String cuit(String texto) {
        Matcher m = CUIT.matcher(texto);
        return m.find() ? m.group(1) + "-" + m.group(2) + "-" + m.group(3) : null;
    }

    private static String proveedor(String texto) {
        String razon = Importes.primero(RAZON_SOCIAL, texto);
        if (razon != null) return razon.trim();
        return texto.lines().map(String::trim)
                .filter(l -> l.chars().filter(Character::isLetter).count() >= 4)
                .filter(l -> !l.matches("(?i).*(original|duplicado|factura|comprobante|c[oó]digo|cod\\.|domicilio|cuit|ingresos).*"))
                .findFirst().orElse(null);
    }

    private static String numero(String texto) {
        Matcher m = NUMERO_AFIP.matcher(texto);
        if (m.find()) {
            return String.format("%05d-%08d", Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
        }
        Matcher g = NUMERO_GUION.matcher(texto);
        return g.find() ? g.group(1) + "-" + g.group(2) : null;
    }

    private static LocalDate fecha(String texto) {
        String f = Importes.primero(FECHA_EMISION, texto);
        if (f == null) return null;
        try {
            return LocalDate.parse(f.replace('-', '/'), DMY);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** El último "Total" de la factura: los anteriores suelen ser parciales. */
    private static BigDecimal total(String texto) {
        Matcher m = TOTAL.matcher(texto);
        BigDecimal ultimo = null;
        while (m.find()) ultimo = Importes.ar(m.group(1));
        return ultimo;
    }

    // ─── Renglones ────────────────────────────────────────────────────────────

    private static List<LineaFactura> renglones(String texto) {
        List<LineaFactura> lineas = new ArrayList<>();
        String anterior = null;     // texto suelto: puede ser la descripción del renglón siguiente
        for (String cruda : texto.split("\\R")) {
            String linea = FECHA_O_CUIT.matcher(cruda).replaceAll(" ").trim();
            if (linea.isEmpty()) continue;
            if (NO_ES_PRODUCTO.matcher(linea).find()) {
                anterior = null;
                continue;
            }
            LineaFactura leida = renglon(linea, anterior);
            if (leida == null && anterior != null) {
                // Renglón partido: la cantidad en una línea y los importes en la siguiente
                leida = renglon(anterior + " " + linea, null);
            }
            if (leida != null) {
                lineas.add(leida);
                anterior = null;
            } else {
                anterior = ENCABEZADO_TABLA.matcher(linea).find() ? null : linea;
            }
        }
        return lineas;
    }

    private static LineaFactura renglon(String linea, String anterior) {
        String[] tokens = linea.split("\\s+");
        List<Integer> posiciones = new ArrayList<>();
        List<BigDecimal> valores = new ArrayList<>();
        for (int i = 0; i < tokens.length; i++) {
            BigDecimal v = numero(tokens, i);
            if (v != null) {
                posiciones.add(i);
                valores.add(v);
            }
        }
        int[] calculo = cantidadPrecioImporte(valores);
        if (calculo == null) return null;

        int cantidad = valores.get(calculo[0]).intValue();
        BigDecimal importe = conIva(valores, calculo[2]);
        int primeroUsado = Math.min(posiciones.get(calculo[0]), posiciones.get(calculo[1]));
        int posImporte = posiciones.get(calculo[2]);

        String codigo = null;
        List<String> palabras = new ArrayList<>();
        for (int i = 0; i < posImporte; i++) {
            String t = tokens[i];
            boolean esNumero = posiciones.contains(i);
            if (codigo == null && ((i <= 1 && esCodigoAlfanumerico(t)) || (esNumero && t.matches("\\d{4,}") && i < primeroUsado))) {
                codigo = t;
            } else if (t.equalsIgnoreCase(codigo) || UNIDADES_DE_MEDIDA.contains(t.toLowerCase(Locale.ROOT))) {
                continue;
            } else if (!esNumero || (i < primeroUsado && t.matches("\\d{1,2}(?:[.,]5)?"))) {
                palabras.add(t);
            }
        }

        String descripcion = String.join(" ", palabras).trim();
        if (cantidadDeLetras(descripcion) < 3) {
            if (anterior == null || cantidadDeLetras(anterior) < 3) return null;
            descripcion = anterior;
        }

        String talle = null;
        Matcher equivalencia = TALLE_CON_EQUIVALENCIA.matcher(descripcion);
        if (equivalencia.find()) {
            if (equivalencia.group(1) != null) talle = equivalencia.group(1).replace(',', '.');
            descripcion = descripcion.substring(0, equivalencia.start()).trim();
        }
        Matcher m = TALLE_ROTULADO.matcher(descripcion);
        if (!m.find()) m = TALLE_AL_FINAL.matcher(descripcion);
        else m.reset();
        if (talle == null && m.find()) {
            talle = m.group(1).replace(',', '.').toUpperCase(Locale.ROOT);
            descripcion = (descripcion.substring(0, m.start()) + descripcion.substring(m.end())).trim();
        }

        if (codigo == null) codigo = codigoDesde(descripcion);
        String sku = talle == null ? codigo : codigo + "-" + talle;
        return new LineaFactura(codigo, descripcion, null, talle == null ? SIN_TALLE : talle, sku, cantidad,
                Importes.porUnidad(importe, cantidad), null, importe);
    }

    private static BigDecimal numero(String[] tokens, int i) {
        String t = tokens[i];
        if (!TOKEN_NUMERO.matcher(t).matches()) return null;
        String limpio = t.replace("$", "").replace("%", "");
        if (limpio.contains(",") || limpio.matches("-?\\d{1,3}(\\.\\d{3})+")) return Importes.ar(limpio);
        return new BigDecimal(limpio);
    }

    /**
     * Busca qué números son cantidad, precio e importe: el importe viene después de los otros dos
     * y es cantidad × precio, con o sin bonificación. Devuelve sus índices, o null.
     */
    private static int[] cantidadPrecioImporte(List<BigDecimal> v) {
        for (int i = v.size() - 1; i >= 2; i--) {
            if (v.get(i).signum() <= 0) continue;
            int[] mejor = null;
            for (int q = 0; q < i; q++) {
                if (!esCantidad(v.get(q))) continue;
                for (int p = 0; p < i; p++) {
                    if (p == q || v.get(p).signum() <= 0) continue;
                    if (!coincide(v, q, p, i)) continue;
                    if (mejor == null || v.get(q).compareTo(v.get(mejor[0])) < 0) {
                        mejor = new int[]{q, p, i};
                    }
                }
            }
            if (mejor != null) return mejor;
        }
        return null;
    }

    private static boolean esCantidad(BigDecimal n) {
        return n.signum() > 0 && n.compareTo(BigDecimal.valueOf(9999)) <= 0
                && n.stripTrailingZeros().scale() <= 0;
    }

    private static boolean coincide(List<BigDecimal> v, int q, int p, int i) {
        BigDecimal bruto = v.get(q).multiply(v.get(p));
        BigDecimal margen = v.get(q).multiply(new BigDecimal("0.01")).add(new BigDecimal("0.02"));
        if (bruto.subtract(v.get(i)).abs().compareTo(margen) <= 0) return true;
        for (int b = 0; b < i; b++) {
            if (b == q || b == p) continue;
            BigDecimal pct = v.get(b);
            if (pct.signum() <= 0 || pct.compareTo(BigDecimal.valueOf(100)) >= 0) continue;
            BigDecimal neto = bruto.multiply(BigDecimal.valueOf(100).subtract(pct))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (neto.subtract(v.get(i)).abs().compareTo(margen) <= 0) return true;
        }
        return false;
    }

    /** Factura A: si después del subtotal viene el importe con IVA, ese es el costo real. */
    private static BigDecimal conIva(List<BigDecimal> v, int i) {
        for (int r = v.size() - 1; r > i; r--) {
            if (esAlicuota(v.get(r), v.get(i))) return v.get(r);
        }
        return v.get(i);
    }

    private static boolean esAlicuota(BigDecimal conIva, BigDecimal neto) {
        BigDecimal margen = new BigDecimal("0.002");
        BigDecimal razon = conIva.divide(neto, 4, RoundingMode.HALF_UP);
        return ALICUOTAS.stream().anyMatch(a -> a.subtract(razon).abs().compareTo(margen) <= 0);
    }

    /** Si los renglones vienen netos y el total los supera justo por el IVA, se les suma. */
    private static List<LineaFactura> ajustarIva(List<LineaFactura> lineas, BigDecimal total) {
        BigDecimal suma = lineas.stream().map(LineaFactura::importe).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (total == null || suma.signum() == 0 || !esAlicuota(total, suma)) return lineas;
        BigDecimal factor = total.divide(suma, 6, RoundingMode.HALF_UP);
        return lineas.stream().map(l -> {
            BigDecimal importe = l.importe().multiply(factor).setScale(2, RoundingMode.HALF_UP);
            return new LineaFactura(l.codigoArticulo(), l.descripcion(), l.marca(), l.talle(), l.skuVariante(),
                    l.cantidad(), Importes.porUnidad(importe, l.cantidad()), l.precioLista(), importe);
        }).toList();
    }

    private static boolean esCodigoAlfanumerico(String t) {
        return t.length() >= 4 && t.matches("[A-Za-z0-9\\-_.]+")
                && t.matches(".*\\d.*") && t.matches(".*[A-Za-z].*");
    }

    private static long cantidadDeLetras(String s) {
        return s.chars().filter(Character::isLetter).count();
    }

    /** Sin código en la factura: se deriva de la descripción, así la próxima compra coincide. */
    private static String codigoDesde(String descripcion) {
        String sinAcentos = Normalizer.normalize(descripcion, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        String slug = sinAcentos.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "-").replaceAll("^-|-$", "");
        return slug.length() > 40 ? slug.substring(0, 40) : slug;
    }
}
