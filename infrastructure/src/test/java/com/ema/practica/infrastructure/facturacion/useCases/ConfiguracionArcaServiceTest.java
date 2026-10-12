package com.ema.practica.infrastructure.facturacion.useCases;

import com.ema.practica.infrastructure.exception.VentaInvalidaException;
import com.ema.practica.infrastructure.facturacion.config.ArcaProperties;
import com.ema.practica.infrastructure.facturacion.crud.ConfiguracionFacturacionRepository;
import com.ema.practica.infrastructure.facturacion.entity.ConfiguracionFacturacionEntity;
import com.ema.practica.infrastructure.facturacion.rest.model.ConfiguracionFacturacionDTO;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.openssl.PEMParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ConfiguracionArcaServiceTest {

    @TempDir Path dir;
    ConfiguracionFacturacionRepository repo = mock(ConfiguracionFacturacionRepository.class);
    ArcaProperties props = new ArcaProperties();
    ConfiguracionArcaService service;
    ConfiguracionFacturacionEntity guardada;

    static final long CUIT = 20172235035L;

    @BeforeEach
    void setUp() {
        props.setDirectorio(dir.toString());
        service = new ConfiguracionArcaService(props, repo);
        when(repo.findById(ConfiguracionFacturacionEntity.ID)).thenAnswer(inv -> Optional.ofNullable(guardada));
        when(repo.save(any())).thenAnswer(inv -> guardada = inv.getArgument(0));
    }

    private ConfiguracionFacturacionDTO datos(long cuit) {
        ConfiguracionFacturacionDTO d = new ConfiguracionFacturacionDTO();
        d.setCuit(cuit);
        d.setPuntoVenta(1);
        d.setRazonSocial("Carlos Ramon Mercado");
        d.setDomicilio("Calle 1");
        d.setInicioActividades("01/03/2020");
        d.setHabilitada(true);
        return d;
    }

    @Test
    void guardar_validaCuitPuntoDeVentaYFecha() {
        assertThatThrownBy(() -> service.guardar(datos(20172235036L), "admin"))
                .isInstanceOf(VentaInvalidaException.class).hasMessageContaining("CUIT inválido");

        ConfiguracionFacturacionDTO d = datos(CUIT);
        d.setPuntoVenta(0);
        assertThatThrownBy(() -> service.guardar(d, "admin")).hasMessageContaining("punto de venta");

        ConfiguracionFacturacionDTO f = datos(CUIT);
        f.setInicioActividades("2020-03-01");
        assertThatThrownBy(() -> service.guardar(f, "admin")).hasMessageContaining("dd/mm/aaaa");

        var dto = service.guardar(datos(CUIT), "admin");
        assertThat(dto.getCuit()).isEqualTo(CUIT);
        assertThat(guardada.getActualizadoPor()).isEqualTo("admin");
    }

    @Test
    void faltantes_vanDesapareciendoHastaQuedarListo() throws Exception {
        assertThat(service.faltantes()).contains("Cargar el CUIT", "Subir el certificado que entrega ARCA");

        service.guardar(datos(CUIT), "admin");
        String csr = service.generarCsr(false);
        assertThat(service.faltantes()).containsExactly("Subir el certificado que entrega ARCA");

        service.instalarCertificado(certificadoDeArca(csr, CUIT, claveGenerada()));
        assertThat(service.faltantes()).isEmpty();
    }

    @Test
    void generarCsr_incluyeCuitYRazonSocial_yLaClaveQuedaEnElServidor() throws Exception {
        service.guardar(datos(CUIT), "admin");

        String csrPem = service.generarCsr(false);

        PKCS10CertificationRequest csr = (PKCS10CertificationRequest) new PEMParser(new StringReader(csrPem)).readObject();
        String sujeto = csr.getSubject().toString();
        assertThat(sujeto).contains("C=AR").contains("O=Carlos Ramon Mercado").contains("CUIT " + CUIT);
        assertThat(Files.readString(dir.resolve("arca.key"))).contains("PRIVATE KEY-----");
        assertThat(csrPem).doesNotContain("PRIVATE KEY");
    }

    @Test
    void generarCsr_conCertificadoInstalado_pideConfirmacion() throws Exception {
        service.guardar(datos(CUIT), "admin");
        service.instalarCertificado(certificadoDeArca(service.generarCsr(false), CUIT, claveGenerada()));

        assertThatThrownBy(() -> service.generarCsr(false)).hasMessageContaining("confirmá");
        service.generarCsr(true);
        assertThat(Files.exists(dir.resolve("arca.crt"))).isFalse(); // el anterior ya no sirve
    }

    @Test
    void instalarCertificado_deOtraClave_falla() throws Exception {
        service.guardar(datos(CUIT), "admin");
        String csr = service.generarCsr(false);
        KeyPairGenerator g = KeyPairGenerator.getInstance("RSA");
        g.initialize(2048);
        KeyPair otra = g.generateKeyPair();

        assertThatThrownBy(() -> service.instalarCertificado(certificadoDeArca(csr, CUIT, otra)))
                .hasMessageContaining("no corresponde a la clave");
    }

    @Test
    void instalarCertificado_deOtroCuit_falla() throws Exception {
        service.guardar(datos(CUIT), "admin");
        String csr = service.generarCsr(false);

        assertThatThrownBy(() -> service.instalarCertificado(certificadoDeArca(csr, 20111111112L, claveGenerada())))
                .hasMessageContaining("es del CUIT 20111111112");
    }

    @Test
    void instalarCertificado_textoInvalido_falla() throws Exception {
        service.guardar(datos(CUIT), "admin");
        service.generarCsr(false);

        assertThatThrownBy(() -> service.instalarCertificado("hola"))
                .hasMessageContaining("no es un certificado válido");
    }

    // ── Simula lo que hace ARCA: firma un certificado para la clave del CSR ──

    private KeyPair claveGenerada() throws Exception {
        PrivateKey priv = ConfiguracionArcaService.leerClave(dir.resolve("arca.key"));
        RSAPrivateCrtKey k = (RSAPrivateCrtKey) priv;
        PublicKey pub = java.security.KeyFactory.getInstance("RSA")
                .generatePublic(new RSAPublicKeySpec(k.getModulus(), k.getPublicExponent()));
        return new KeyPair(pub, priv);
    }

    private static String certificadoDeArca(String csrIgnorado, long cuit, KeyPair kp) throws Exception {
        X500Name sujeto = new X500Name("C=AR, O=Prueba, CN=tienda, SERIALNUMBER=CUIT " + cuit);
        X500Name emisor = new X500Name("C=AR, O=AFIP, CN=Computadores Test");
        Date desde = new Date(System.currentTimeMillis() - 60_000);
        Date hasta = new Date(System.currentTimeMillis() + 2L * 365 * 24 * 3600 * 1000);
        var builder = new JcaX509v3CertificateBuilder(emisor, BigInteger.ONE, desde, hasta, sujeto, kp.getPublic());
        X509Certificate cert = new JcaX509CertificateConverter().getCertificate(
                builder.build(new JcaContentSignerBuilder("SHA256withRSA").build(kp.getPrivate())));
        StringWriter sw = new StringWriter();
        try (JcaPEMWriter w = new JcaPEMWriter(sw)) {
            w.writeObject(cert);
        }
        return sw.toString();
    }
}
