package com.ema.practica.infrastructure.facturacion.arca;

import com.ema.practica.infrastructure.facturacion.config.ArcaProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

/**
 * WSAA: autenticación con certificado. Devuelve token + sign para el servicio
 * "wsfe", válidos 12 h. Se reutilizan (memoria y disco) porque ARCA rechaza
 * pedir un ticket nuevo mientras el anterior está vigente.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WsaaClient {

    static final String SERVICIO = "wsfe";
    private static final String NS = "http://wsaa.view.sua.dvadac.desein.afip.gov";
    private static final ZoneId AR = ZoneId.of("America/Argentina/Buenos_Aires");

    public record Ticket(String token, String sign, OffsetDateTime expiracion) {
        boolean vigente() {
            // margen de 10 min para no usar un ticket a punto de vencer
            return expiracion.isAfter(OffsetDateTime.now().plusMinutes(10));
        }
    }

    private final ArcaProperties props;
    private final SoapHttp soap;
    private Ticket ticket;

    public synchronized Ticket ticket() {
        if (ticket != null && ticket.vigente()) return ticket;
        Ticket guardado = leerCache();
        if (guardado != null && guardado.vigente()) {
            ticket = guardado;
            return ticket;
        }
        ticket = solicitar();
        guardarCache(ticket);
        return ticket;
    }

    private Ticket solicitar() {
        String cms = firmar(tra());
        Document doc = soap.call(props.wsaaUrlEfectiva(), "",
                "<loginCms xmlns=\"" + NS + "\"><in0>" + cms + "</in0></loginCms>");
        String ret = SoapHttp.text(doc, "loginCmsReturn");
        if (ret == null) throw new ArcaException("WSAA no devolvió el ticket de acceso");

        Document ta = SoapHttp.parse(ret);
        String token = SoapHttp.text(ta, "token");
        String sign = SoapHttp.text(ta, "sign");
        String exp = SoapHttp.text(ta, "expirationTime");
        if (token == null || sign == null || exp == null) {
            throw new ArcaException("Ticket de acceso de ARCA incompleto");
        }
        log.info("ARCA: nuevo ticket de acceso ({}) válido hasta {}", props.getAmbiente(), exp);
        return new Ticket(token, sign, OffsetDateTime.parse(exp));
    }

    /** Ticket de Requerimiento de Acceso (TRA) */
    String tra() {
        OffsetDateTime ahora = OffsetDateTime.now(AR);
        DateTimeFormatter f = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<loginTicketRequest version=\"1.0\"><header>"
                + "<uniqueId>" + (System.currentTimeMillis() / 1000) + "</uniqueId>"
                + "<generationTime>" + ahora.minusMinutes(10).withNano(0).format(f) + "</generationTime>"
                + "<expirationTime>" + ahora.plusMinutes(10).withNano(0).format(f) + "</expirationTime>"
                + "</header><service>" + SERVICIO + "</service></loginTicketRequest>";
    }

    /** Firma el TRA como CMS (PKCS#7 SignedData con el contenido incluido) en Base64 */
    String firmar(String tra) {
        try {
            X509Certificate cert = leerCertificado(Path.of(props.getCertPath()));
            PrivateKey key = leerClave(Path.of(props.getKeyPath()));
            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
            gen.addSignerInfoGenerator(new JcaSimpleSignerInfoGeneratorBuilder()
                    .build("SHA256withRSA", key, cert));
            gen.addCertificates(new JcaCertStore(List.of(cert)));
            byte[] cms = gen.generate(new CMSProcessableByteArray(tra.getBytes(StandardCharsets.UTF_8)), true)
                    .getEncoded();
            return Base64.getEncoder().encodeToString(cms);
        } catch (ArcaException e) {
            throw e;
        } catch (Exception e) {
            throw new ArcaException("No se pudo firmar el pedido a ARCA con el certificado configurado: "
                    + e.getMessage(), e);
        }
    }

    private static X509Certificate leerCertificado(Path path) throws Exception {
        if (!Files.exists(path)) throw new ArcaException("No existe el certificado: " + path);
        try (InputStream in = new FileInputStream(path.toFile())) {
            return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(in);
        }
    }

    // Acepta PKCS#1 ("BEGIN RSA PRIVATE KEY", openssl genrsa) y PKCS#8 ("BEGIN PRIVATE KEY")
    private static PrivateKey leerClave(Path path) throws Exception {
        if (!Files.exists(path)) throw new ArcaException("No existe la clave privada: " + path);
        try (Reader r = new FileReader(path.toFile(), StandardCharsets.UTF_8);
             PEMParser parser = new PEMParser(r)) {
            Object obj = parser.readObject();
            JcaPEMKeyConverter conv = new JcaPEMKeyConverter();
            if (obj instanceof PEMKeyPair kp) return conv.getPrivateKey(kp.getPrivateKeyInfo());
            if (obj instanceof PrivateKeyInfo pki) return conv.getPrivateKey(pki);
            throw new ArcaException("La clave privada no está en formato PEM sin contraseña");
        }
    }

    // ── Caché en disco del ticket ─────────────────────────────────────────────

    private Path cacheFile() {
        return Path.of(props.getTaCacheDir(),
                "ta-" + props.getCuit() + "-" + props.getAmbiente().name().toLowerCase() + ".properties");
    }

    private Ticket leerCache() {
        try {
            Path f = cacheFile();
            if (!Files.exists(f)) return null;
            Properties p = new Properties();
            try (Reader r = Files.newBufferedReader(f)) {
                p.load(r);
            }
            return new Ticket(p.getProperty("token"), p.getProperty("sign"),
                    OffsetDateTime.parse(p.getProperty("expiracion")));
        } catch (Exception e) {
            log.warn("No se pudo leer el ticket de ARCA guardado: {}", e.getMessage());
            return null;
        }
    }

    private void guardarCache(Ticket t) {
        try {
            Path f = cacheFile();
            Files.createDirectories(f.getParent());
            Properties p = new Properties();
            p.setProperty("token", t.token());
            p.setProperty("sign", t.sign());
            p.setProperty("expiracion", t.expiracion().toString());
            try (var w = Files.newBufferedWriter(f)) {
                p.store(w, "Ticket de acceso ARCA (WSAA) - no compartir");
            }
        } catch (Exception e) {
            log.warn("No se pudo guardar el ticket de ARCA: {}", e.getMessage());
        }
    }
}
