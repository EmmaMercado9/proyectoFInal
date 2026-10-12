package com.ema.practica.infrastructure.facturacion.useCases;

import com.ema.practica.infrastructure.exception.VentaInvalidaException;
import com.ema.practica.infrastructure.facturacion.config.ArcaProperties;
import com.ema.practica.infrastructure.facturacion.crud.ConfiguracionFacturacionRepository;
import com.ema.practica.infrastructure.facturacion.entity.ConfiguracionFacturacionEntity;
import com.ema.practica.infrastructure.facturacion.rest.model.ConfiguracionFacturacionDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Datos del emisor (base de datos) y certificado de ARCA (archivos en el
 * servidor). La clave privada se genera acá y nunca sale del servidor:
 * el panel solo descarga el CSR y sube el .crt que entrega ARCA.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfiguracionArcaService {

    private static final DateTimeFormatter DDMMYYYY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ArcaProperties props;
    private final ConfiguracionFacturacionRepository repo;

    public record Emisor(boolean habilitada, Long cuit, Integer puntoVenta, String razonSocial,
                         String domicilio, String iibb, String inicioActividades, String condicionIva) {}

    public record Certificado(String sujeto, Long cuit, LocalDate vence, boolean coincideConClave) {
        public boolean vencido() {
            return vence.isBefore(LocalDate.now());
        }
    }

    // ── Lectura ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Emisor emisor() {
        ConfiguracionFacturacionEntity c = entidad();
        return new Emisor(c.isHabilitada(), c.getCuit(), c.getPuntoVenta(), c.getRazonSocial(),
                c.getDomicilio(), c.getIibb(), c.getInicioActividades(), c.getCondicionIva());
    }

    public Path keyFile() {
        return blank(props.getKeyPath()) ? Path.of(props.getDirectorio(), "arca.key") : Path.of(props.getKeyPath());
    }

    public Path certFile() {
        return blank(props.getCertPath()) ? Path.of(props.getDirectorio(), "arca.crt") : Path.of(props.getCertPath());
    }

    Path csrFile() {
        return Path.of(props.getDirectorio(), "arca.csr");
    }

    /** true si el certificado/clave los fija el servidor por variables de entorno */
    public boolean archivosDelServidor() {
        return !blank(props.getCertPath()) || !blank(props.getKeyPath());
    }

    public Optional<Certificado> certificado() {
        Path f = certFile();
        if (!Files.exists(f)) return Optional.empty();
        try {
            return Optional.of(analizar(leerCertificado(Files.readString(f, StandardCharsets.UTF_8))));
        } catch (Exception e) {
            log.warn("Certificado de ARCA ilegible: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /** Qué falta para poder facturar, en lenguaje del usuario (vacío = listo) */
    @Transactional(readOnly = true)
    public List<String> faltantes() {
        Emisor e = emisor();
        List<String> f = new ArrayList<>();
        if (props.produccionBloqueada()) {
            f.add("Producción bloqueada en el servidor (ARCA_PERMITIR_PRODUCCION): solo se factura en prueba");
        }
        if (e.cuit() == null) f.add("Cargar el CUIT");
        if (e.puntoVenta() == null) f.add("Cargar el punto de venta");
        if (blank(e.razonSocial())) f.add("Cargar el nombre o razón social");
        if (!Files.exists(keyFile())) f.add("Generar la clave y el pedido de certificado (CSR)");
        Optional<Certificado> cert = certificado();
        if (cert.isEmpty()) {
            f.add("Subir el certificado que entrega ARCA");
        } else {
            Certificado c = cert.get();
            if (!c.coincideConClave()) f.add("El certificado no corresponde a la clave generada");
            if (e.cuit() != null && c.cuit() != null && !c.cuit().equals(e.cuit())) {
                f.add("El certificado es del CUIT " + c.cuit() + " y el configurado es " + e.cuit());
            }
            if (c.vencido()) f.add("El certificado venció el " + c.vence().format(DDMMYYYY));
        }
        if (!e.habilitada()) f.add("Activar la facturación");
        return f;
    }

    @Transactional(readOnly = true)
    public ConfiguracionFacturacionDTO dto() {
        ConfiguracionFacturacionEntity c = entidad();
        ConfiguracionFacturacionDTO dto = new ConfiguracionFacturacionDTO();
        dto.setHabilitada(c.isHabilitada());
        dto.setCuit(c.getCuit());
        dto.setPuntoVenta(c.getPuntoVenta());
        dto.setRazonSocial(c.getRazonSocial());
        dto.setDomicilio(c.getDomicilio());
        dto.setIibb(c.getIibb());
        dto.setInicioActividades(c.getInicioActividades());
        dto.setCondicionIva(c.getCondicionIva());
        dto.setActualizadoPor(c.getActualizadoPor());
        dto.setActualizadoEn(c.getActualizadoEn());
        dto.setAmbiente(props.getAmbiente().name());
        dto.setProduccionBloqueada(props.produccionBloqueada());
        dto.setArchivosDelServidor(archivosDelServidor());
        dto.setClaveGenerada(Files.exists(keyFile()));
        dto.setCsrDisponible(Files.exists(csrFile()));
        certificado().ifPresent(cert -> {
            dto.setCertificadoSujeto(cert.sujeto());
            dto.setCertificadoCuit(cert.cuit());
            dto.setCertificadoVence(cert.vence());
            dto.setCertificadoCoincide(cert.coincideConClave());
        });
        dto.setFaltantes(faltantes());
        return dto;
    }

    // ── Escritura ───────────────────────────────────────────────────────────

    @Transactional
    public ConfiguracionFacturacionDTO guardar(ConfiguracionFacturacionDTO req, String usuario) {
        if (req.getCuit() == null || !FacturacionService.cuitValido(req.getCuit())) {
            throw new VentaInvalidaException("CUIT inválido (revisá el dígito verificador)");
        }
        if (req.getPuntoVenta() == null || req.getPuntoVenta() < 1 || req.getPuntoVenta() > 99_998) {
            throw new VentaInvalidaException("El punto de venta debe estar entre 1 y 99998");
        }
        if (blank(req.getRazonSocial())) {
            throw new VentaInvalidaException("Cargá el nombre o razón social");
        }
        if (!blank(req.getInicioActividades())) {
            try {
                LocalDate.parse(req.getInicioActividades().trim(), DDMMYYYY);
            } catch (DateTimeParseException e) {
                throw new VentaInvalidaException("Inicio de actividades: usá el formato dd/mm/aaaa");
            }
        }

        ConfiguracionFacturacionEntity c = entidad();
        c.setCuit(req.getCuit());
        c.setPuntoVenta(req.getPuntoVenta());
        c.setRazonSocial(req.getRazonSocial().trim());
        c.setDomicilio(trimOrNull(req.getDomicilio()));
        c.setIibb(trimOrNull(req.getIibb()));
        c.setInicioActividades(trimOrNull(req.getInicioActividades()));
        c.setHabilitada(req.isHabilitada());
        c.setActualizadoPor(usuario);
        c.setActualizadoEn(LocalDateTime.now());
        repo.save(c);
        return dto();
    }

    /**
     * Genera una clave RSA 2048 nueva (queda en el servidor) y devuelve el CSR
     * para subir a ARCA. Si ya hay un certificado, generar otra clave lo deja
     * inservible: por eso pide confirmación explícita (reemplazar = true).
     */
    public String generarCsr(boolean reemplazar) {
        if (archivosDelServidor()) {
            throw new VentaInvalidaException("El certificado lo define el servidor (ARCA_CERT_PATH / ARCA_KEY_PATH)");
        }
        Emisor e = emisor();
        if (e.cuit() == null || blank(e.razonSocial())) {
            throw new VentaInvalidaException("Primero guardá el CUIT y el nombre o razón social");
        }
        if (Files.exists(certFile()) && !reemplazar) {
            throw new VentaInvalidaException(
                    "Ya hay un certificado instalado. Generar una clave nueva lo invalida: confirmá para continuar");
        }
        try {
            Files.createDirectories(Path.of(props.getDirectorio()));
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            KeyPair kp = gen.generateKeyPair();

            X500Name sujeto = new X500NameBuilder(BCStyle.INSTANCE)
                    .addRDN(BCStyle.C, "AR")
                    .addRDN(BCStyle.O, e.razonSocial())
                    .addRDN(BCStyle.CN, "tienda")
                    .addRDN(BCStyle.SERIALNUMBER, "CUIT " + e.cuit())
                    .build();
            PKCS10CertificationRequest csr = new JcaPKCS10CertificationRequestBuilder(sujeto, kp.getPublic())
                    .build(new JcaContentSignerBuilder("SHA256withRSA").build(kp.getPrivate()));

            String keyPem = pem(kp.getPrivate());
            String csrPem = pem(csr);
            Files.writeString(keyFile(), keyPem, StandardCharsets.UTF_8);
            restringirPermisos(keyFile());
            Files.writeString(csrFile(), csrPem, StandardCharsets.UTF_8);
            Files.deleteIfExists(certFile()); // el certificado anterior ya no corresponde
            log.info("ARCA: nueva clave y CSR generados para CUIT {}", e.cuit());
            return csrPem;
        } catch (VentaInvalidaException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new VentaInvalidaException("No se pudo generar el pedido de certificado: " + ex.getMessage());
        }
    }

    public String csrActual() {
        try {
            return Files.readString(csrFile(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new VentaInvalidaException("Todavía no se generó el pedido de certificado");
        }
    }

    /** Valida el .crt de ARCA contra la clave y el CUIT configurados y lo instala */
    public ConfiguracionFacturacionDTO instalarCertificado(String pem) {
        if (archivosDelServidor()) {
            throw new VentaInvalidaException("El certificado lo define el servidor (ARCA_CERT_PATH / ARCA_KEY_PATH)");
        }
        if (!Files.exists(keyFile())) {
            throw new VentaInvalidaException("Primero generá la clave y el pedido de certificado");
        }
        X509Certificate cert;
        try {
            cert = leerCertificado(pem);
        } catch (Exception e) {
            throw new VentaInvalidaException("El archivo no es un certificado válido (.crt en formato PEM)");
        }
        Certificado info = analizar(cert);
        if (!info.coincideConClave()) {
            throw new VentaInvalidaException(
                    "El certificado no corresponde a la clave de este sistema: subí el que ARCA generó con el último CSR");
        }
        Long cuit = emisor().cuit();
        if (cuit != null && info.cuit() != null && !info.cuit().equals(cuit)) {
            throw new VentaInvalidaException("El certificado es del CUIT " + info.cuit()
                    + " y el configurado es " + cuit);
        }
        if (info.vencido()) {
            throw new VentaInvalidaException("El certificado está vencido (" + info.vence().format(DDMMYYYY) + ")");
        }
        try {
            Files.writeString(certFile(), pem.trim() + "\n", StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new VentaInvalidaException("No se pudo guardar el certificado: " + e.getMessage());
        }
        log.info("ARCA: certificado instalado ({}), vence {}", info.sujeto(), info.vence());
        return dto();
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private ConfiguracionFacturacionEntity entidad() {
        return repo.findById(ConfiguracionFacturacionEntity.ID).orElseGet(ConfiguracionFacturacionEntity::new);
    }

    private static X509Certificate leerCertificado(String pem) throws Exception {
        return (X509Certificate) CertificateFactory.getInstance("X.509")
                .generateCertificate(new ByteArrayInputStream(pem.trim().getBytes(StandardCharsets.UTF_8)));
    }

    private Certificado analizar(X509Certificate cert) {
        X500Name sujeto;
        try {
            sujeto = new JcaX509CertificateHolder(cert).getSubject();
        } catch (Exception e) {
            throw new VentaInvalidaException("No se pudo leer el certificado");
        }
        Long cuit = null;
        RDN[] serial = sujeto.getRDNs(BCStyle.SERIALNUMBER);
        if (serial.length > 0) {
            String v = IETFUtils.valueToString(serial[0].getFirst().getValue()).replaceAll("\\D", "");
            if (v.length() == 11) cuit = Long.parseLong(v);
        }
        LocalDate vence = cert.getNotAfter().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return new Certificado(sujeto.toString(), cuit, vence, coincideConClave(cert));
    }

    /** La clave pública del certificado tiene que ser la de nuestra clave privada */
    private boolean coincideConClave(X509Certificate cert) {
        try {
            PrivateKey key = leerClave(keyFile());
            return key instanceof RSAPrivateCrtKey k && cert.getPublicKey() instanceof RSAPublicKey p
                    && k.getModulus().equals(p.getModulus()) && k.getPublicExponent().equals(p.getPublicExponent());
        } catch (Exception e) {
            return false;
        }
    }

    static PrivateKey leerClave(Path path) throws Exception {
        try (Reader r = Files.newBufferedReader(path, StandardCharsets.UTF_8); PEMParser parser = new PEMParser(r)) {
            Object obj = parser.readObject();
            JcaPEMKeyConverter conv = new JcaPEMKeyConverter();
            if (obj instanceof PEMKeyPair kp) return conv.getPrivateKey(kp.getPrivateKeyInfo());
            if (obj instanceof PrivateKeyInfo pki) return conv.getPrivateKey(pki);
            throw new IllegalArgumentException("La clave privada no está en formato PEM sin contraseña");
        }
    }

    private static String pem(Object o) throws Exception {
        StringWriter sw = new StringWriter();
        try (JcaPEMWriter w = new JcaPEMWriter(sw)) {
            w.writeObject(o);
        }
        return sw.toString();
    }

    // Solo el dueño puede leer la clave (en Windows es best effort)
    private static void restringirPermisos(Path f) {
        var file = f.toFile();
        file.setReadable(false, false);
        file.setReadable(true, true);
        file.setWritable(false, false);
        file.setWritable(true, true);
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }

    private static String trimOrNull(String s) {
        return blank(s) ? null : s.trim();
    }
}
