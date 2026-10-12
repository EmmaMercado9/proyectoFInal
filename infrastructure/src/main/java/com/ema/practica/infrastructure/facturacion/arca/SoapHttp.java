package com.ema.practica.infrastructure.facturacion.arca;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Transporte SOAP 1.1 mínimo (HTTP + DOM). ARCA usa SOAP simple, así que
 * evitamos generar clientes desde el WSDL.
 */
@Component
public class SoapHttp {

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    /** Envía el body (sin Envelope) y devuelve la respuesta parseada. Los SOAP Fault se lanzan como ArcaException. */
    public Document call(String url, String soapAction, String bodyXml) {
        String envelope = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                + "<soap:Body>" + bodyXml + "</soap:Body></soap:Envelope>";
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(40))
                .header("Content-Type", "text/xml; charset=utf-8")
                .header("SOAPAction", "\"" + soapAction + "\"")
                .POST(HttpRequest.BodyPublishers.ofString(envelope, StandardCharsets.UTF_8))
                .build();
        String respuesta;
        try {
            respuesta = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)).body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ArcaException("Se interrumpió la comunicación con ARCA", e);
        } catch (Exception e) {
            throw new ArcaException("No se pudo conectar con ARCA (" + url + "): " + e.getMessage(), e);
        }
        Document doc = parse(respuesta);
        String fault = text(doc.getDocumentElement(), "faultstring");
        if (fault != null) {
            throw new ArcaException("ARCA respondió con error: " + fault + pista(fault));
        }
        return doc;
    }

    /** Qué hacer ante los errores más comunes de configuración en ARCA */
    static String pista(String fault) {
        String f = fault.toLowerCase();
        if (f.contains("no autorizado a acceder")) {
            return ". Falta autorizar el certificado para el servicio wsfe "
                    + "(ARCA → WSASS → \"Crear autorización a servicio\").";
        }
        if (f.contains("ac de confianza")) {
            return ". El certificado no es de ARCA o es de otro ambiente (prueba/producción).";
        }
        if (f.contains("ya posee un ta valido")) {
            return ". Ya hay un ticket vigente: esperá unos minutos y reintentá.";
        }
        return "";
    }

    public static Document parse(String xml) {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(true);
            f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            return f.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
        } catch (Exception e) {
            throw new ArcaException("Respuesta de ARCA inválida: " + e.getMessage(), e);
        }
    }

    /** Texto del primer descendiente con ese nombre local (ignora namespaces), o null */
    public static String text(Node parent, String localName) {
        Element e = first(parent, localName);
        return e != null ? e.getTextContent().trim() : null;
    }

    public static Element first(Node parent, String localName) {
        NodeList list = parent instanceof Document d
                ? d.getElementsByTagNameNS("*", localName)
                : ((Element) parent).getElementsByTagNameNS("*", localName);
        return list.getLength() > 0 ? (Element) list.item(0) : null;
    }

    public static NodeList all(Node parent, String localName) {
        return parent instanceof Document d
                ? d.getElementsByTagNameNS("*", localName)
                : ((Element) parent).getElementsByTagNameNS("*", localName);
    }

    public static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }
}
