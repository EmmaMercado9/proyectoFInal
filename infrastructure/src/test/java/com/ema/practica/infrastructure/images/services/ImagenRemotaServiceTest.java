package com.ema.practica.infrastructure.images.services;

import com.ema.practica.infrastructure.exception.VentaInvalidaException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.InetAddress;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImagenRemotaServiceTest {

    private final ImagenRemotaService service = new ImagenRemotaService();

    @ParameterizedTest
    @ValueSource(strings = {
            "http://localhost:8080/api", "http://127.0.0.1/a.png", "http://10.0.0.5/a.png",
            "http://192.168.1.1/a.png", "http://169.254.169.254/latest", "http://[::1]/a.png",
            "file:///etc/passwd", "ftp://ejemplo.com/a.png", "http://8.8.8.8:3306/a.png", "no es un link"})
    void rechazaDireccionesInternasYEsquemasRaros(String link) {
        assertThatThrownBy(() -> service.validar(link)).isInstanceOf(VentaInvalidaException.class);
    }

    @Test
    void aceptaUnaDireccionPublica() {
        assertThat(service.validar("https://8.8.8.8/foto.jpg").getHost()).isEqualTo("8.8.8.8");
    }

    @Test
    void redCompartidaEIpv6PrivadaNoSonPublicas() throws Exception {
        assertThat(ImagenRemotaService.esPublica(InetAddress.getByName("100.64.0.1"))).isFalse();
        assertThat(ImagenRemotaService.esPublica(InetAddress.getByName("fd00::1"))).isFalse();
        assertThat(ImagenRemotaService.esPublica(InetAddress.getByName("1.1.1.1"))).isTrue();
    }

    @Test
    void reconoceElTipoPorElContenido() {
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 0, 0, 0, 0, 0, 0, 0, 0, 0};
        byte[] html = "<html><body>pagina</body></html>".getBytes();

        assertThat(ImagenRemotaService.tipoDe(png)).isEqualTo("image/png");
        assertThatThrownBy(() -> ImagenRemotaService.tipoDe(html)).isInstanceOf(VentaInvalidaException.class);
    }
}
