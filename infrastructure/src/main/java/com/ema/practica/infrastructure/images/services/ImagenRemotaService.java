package com.ema.practica.infrastructure.images.services;

import com.ema.practica.infrastructure.exception.VentaInvalidaException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;

/**
 * Trae una imagen de internet a partir de su link, para cargarla en un producto.
 * El servidor hace el pedido, así que solo se permiten direcciones públicas:
 * nunca la propia máquina ni la red interna.
 */
@Service
public class ImagenRemotaService {

    static final int TAMANIO_MAXIMO = 2 * 1024 * 1024;
    private static final int MAX_REDIRECCIONES = 3;

    public record ImagenRemota(String tipo, String base64) {
    }

    private final HttpClient cliente = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .followRedirects(HttpClient.Redirect.NEVER)   // cada salto se valida a mano
            .build();

    public ImagenRemota descargar(String link) {
        URI uri = validar(link);
        try {
            for (int i = 0; i <= MAX_REDIRECCIONES; i++) {
                HttpRequest pedido = HttpRequest.newBuilder(uri)
                        .timeout(Duration.ofSeconds(15))
                        .header("User-Agent", "Mozilla/5.0")
                        .header("Accept", "image/*")
                        .GET().build();
                HttpResponse<InputStream> r = cliente.send(pedido, HttpResponse.BodyHandlers.ofInputStream());
                try (InputStream cuerpo = r.body()) {
                    if (r.statusCode() / 100 == 3) {
                        String destino = r.headers().firstValue("Location")
                                .orElseThrow(() -> new VentaInvalidaException("No se pudo descargar la imagen."));
                        uri = validar(uri.resolve(destino).toString());
                        continue;
                    }
                    if (r.statusCode() != 200) {
                        throw new VentaInvalidaException("Ese sitio no deja descargar la imagen. Probá copiando la imagen y pegándola.");
                    }
                    byte[] datos = cuerpo.readNBytes(TAMANIO_MAXIMO + 1);
                    if (datos.length > TAMANIO_MAXIMO) {
                        throw new VentaInvalidaException("La imagen supera los 2 MB.");
                    }
                    return new ImagenRemota(tipoDe(datos), Base64.getEncoder().encodeToString(datos));
                }
            }
            throw new VentaInvalidaException("El link redirige demasiadas veces.");
        } catch (IOException e) {
            throw new VentaInvalidaException("No se pudo descargar la imagen de ese link.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new VentaInvalidaException("Se interrumpió la descarga de la imagen.");
        }
    }

    /** El tipo sale del contenido real del archivo, no de lo que declara el sitio. */
    static String tipoDe(byte[] d) {
        if (d.length > 12) {
            if ((d[0] & 0xFF) == 0xFF && (d[1] & 0xFF) == 0xD8 && (d[2] & 0xFF) == 0xFF) return "image/jpeg";
            if ((d[0] & 0xFF) == 0x89 && d[1] == 'P' && d[2] == 'N' && d[3] == 'G') return "image/png";
            if (d[0] == 'R' && d[1] == 'I' && d[2] == 'F' && d[3] == 'F'
                    && d[8] == 'W' && d[9] == 'E' && d[10] == 'B' && d[11] == 'P') return "image/webp";
        }
        throw new VentaInvalidaException("Ese link no es una imagen JPG, PNG o WEBP. Copiá la dirección de la imagen, no la de la página.");
    }

    URI validar(String link) {
        URI uri;
        try {
            uri = URI.create(link == null ? "" : link.trim());
        } catch (IllegalArgumentException e) {
            throw new VentaInvalidaException("El link no es válido.");
        }
        String esquema = uri.getScheme();
        if (uri.getHost() == null || !("http".equalsIgnoreCase(esquema) || "https".equalsIgnoreCase(esquema))
                || !(uri.getPort() == -1 || uri.getPort() == 80 || uri.getPort() == 443)) {
            throw new VentaInvalidaException("El link no es válido.");
        }
        try {
            for (InetAddress ip : resolver(uri.getHost())) {
                if (!esPublica(ip)) throw new VentaInvalidaException("Ese link no está permitido.");
            }
        } catch (UnknownHostException e) {
            throw new VentaInvalidaException("No se encontró el sitio de ese link.");
        }
        return uri;
    }

    InetAddress[] resolver(String host) throws UnknownHostException {
        return InetAddress.getAllByName(host);
    }

    static boolean esPublica(InetAddress ip) {
        if (ip.isLoopbackAddress() || ip.isAnyLocalAddress() || ip.isLinkLocalAddress()
                || ip.isSiteLocalAddress() || ip.isMulticastAddress()) {
            return false;
        }
        byte[] b = ip.getAddress();
        if (b.length == 4) {
            int a0 = b[0] & 0xFF, a1 = b[1] & 0xFF;
            return a0 != 0 && !(a0 == 100 && a1 >= 64 && a1 <= 127);   // red compartida de operadores
        }
        return (b[0] & 0xFE) != 0xFC;   // IPv6 privada (fc00::/7)
    }
}
