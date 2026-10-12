package com.ema.practica.infrastructure.facturacion.arca;

import com.ema.practica.infrastructure.facturacion.config.ArcaProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * WSFEv1 (RG 2485): autorización de comprobantes sin detalle de ítems.
 * El orden de los elementos respeta el WSDL (FEDetRequest).
 */
@Component
@RequiredArgsConstructor
public class WsfeClient {

    private static final String NS = "http://ar.gov.afip.dif.FEV1/";
    static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final ArcaProperties props;
    private final WsaaClient wsaa;
    private final SoapHttp soap;

    /** Comprobante asociado (obligatorio en notas de crédito) */
    public record CbteAsociado(int tipo, int ptoVta, long nro, long cuit, LocalDate fecha) {}

    public record SolicitudCae(int cbteTipo, int ptoVta, long numero, LocalDate fecha,
                               int docTipo, long docNro, BigDecimal importeTotal,
                               int condicionIvaReceptorId, CbteAsociado asociado) {}

    public record ResultadoCae(boolean aprobado, String cae, LocalDate caeVencimiento,
                               List<String> observaciones, List<String> errores) {}

    /** Estado de los servidores de ARCA (no requiere autenticación) */
    public Map<String, String> dummy() {
        Document doc = soap.call(props.wsfeUrlEfectiva(), NS + "FEDummy", "<FEDummy xmlns=\"" + NS + "\"/>");
        Map<String, String> estado = new LinkedHashMap<>();
        for (String k : List.of("AppServer", "DbServer", "AuthServer")) {
            estado.put(k, SoapHttp.text(doc, k));
        }
        return estado;
    }

    public long ultimoAutorizado(int ptoVta, int cbteTipo) {
        Document doc = soap.call(props.wsfeUrlEfectiva(), NS + "FECompUltimoAutorizado",
                "<FECompUltimoAutorizado xmlns=\"" + NS + "\">" + auth()
                        + "<PtoVta>" + ptoVta + "</PtoVta><CbteTipo>" + cbteTipo + "</CbteTipo>"
                        + "</FECompUltimoAutorizado>");
        List<String> errores = mensajes(doc, "Err");
        if (!errores.isEmpty()) {
            throw new ArcaException("ARCA no informó el último comprobante: " + String.join(" · ", errores));
        }
        String nro = SoapHttp.text(doc, "CbteNro");
        return nro != null ? Long.parseLong(nro) : 0L;
    }

    public ResultadoCae solicitarCae(SolicitudCae s) {
        Document doc = soap.call(props.wsfeUrlEfectiva(), NS + "FECAESolicitar", body(s));
        return parsearResultado(doc);
    }

    // ── XML ─────────────────────────────────────────────────────────────────

    String body(SolicitudCae s) {
        String total = importe(s.importeTotal());
        StringBuilder det = new StringBuilder()
                .append("<Concepto>1</Concepto>")                       // 1 = productos
                .append("<DocTipo>").append(s.docTipo()).append("</DocTipo>")
                .append("<DocNro>").append(s.docNro()).append("</DocNro>")
                .append("<CbteDesde>").append(s.numero()).append("</CbteDesde>")
                .append("<CbteHasta>").append(s.numero()).append("</CbteHasta>")
                .append("<CbteFch>").append(s.fecha().format(YYYYMMDD)).append("</CbteFch>")
                // Comprobantes C: sin IVA discriminado → neto = total
                .append("<ImpTotal>").append(total).append("</ImpTotal>")
                .append("<ImpTotConc>0</ImpTotConc>")
                .append("<ImpNeto>").append(total).append("</ImpNeto>")
                .append("<ImpOpEx>0</ImpOpEx>")
                .append("<ImpTrib>0</ImpTrib>")
                .append("<ImpIVA>0</ImpIVA>")
                .append("<MonId>PES</MonId>")
                .append("<MonCotiz>1</MonCotiz>")
                .append("<CondicionIVAReceptorId>").append(s.condicionIvaReceptorId())
                .append("</CondicionIVAReceptorId>");
        if (s.asociado() != null) {
            CbteAsociado a = s.asociado();
            det.append("<CbtesAsoc><CbteAsoc>")
                    .append("<Tipo>").append(a.tipo()).append("</Tipo>")
                    .append("<PtoVta>").append(a.ptoVta()).append("</PtoVta>")
                    .append("<Nro>").append(a.nro()).append("</Nro>")
                    .append("<Cuit>").append(a.cuit()).append("</Cuit>")
                    .append("<CbteFch>").append(a.fecha().format(YYYYMMDD)).append("</CbteFch>")
                    .append("</CbteAsoc></CbtesAsoc>");
        }
        return "<FECAESolicitar xmlns=\"" + NS + "\">" + auth()
                + "<FeCAEReq><FeCabReq><CantReg>1</CantReg>"
                + "<PtoVta>" + s.ptoVta() + "</PtoVta><CbteTipo>" + s.cbteTipo() + "</CbteTipo>"
                + "</FeCabReq><FeDetReq><FECAEDetRequest>" + det + "</FECAEDetRequest></FeDetReq>"
                + "</FeCAEReq></FECAESolicitar>";
    }

    static ResultadoCae parsearResultado(Document doc) {
        List<String> errores = mensajes(doc, "Err");
        List<String> obs = mensajes(doc, "Obs");
        Element det = SoapHttp.first(doc, "FECAEDetResponse");
        String resultado = det != null ? SoapHttp.text(det, "Resultado") : null;
        String cae = det != null ? SoapHttp.text(det, "CAE") : null;
        String vto = det != null ? SoapHttp.text(det, "CAEFchVto") : null;
        boolean aprobado = "A".equals(resultado) && cae != null && !cae.isBlank();
        LocalDate vencimiento = vto != null && !vto.isBlank() ? LocalDate.parse(vto, YYYYMMDD) : null;
        return new ResultadoCae(aprobado, aprobado ? cae : null, vencimiento, obs, errores);
    }

    private String auth() {
        WsaaClient.Ticket t = wsaa.ticket();
        return "<Auth><Token>" + SoapHttp.esc(t.token()) + "</Token><Sign>" + SoapHttp.esc(t.sign())
                + "</Sign><Cuit>" + props.getCuit() + "</Cuit></Auth>";
    }

    private static List<String> mensajes(Document doc, String tag) {
        List<String> out = new ArrayList<>();
        NodeList nodes = SoapHttp.all(doc, tag);
        for (int i = 0; i < nodes.getLength(); i++) {
            Element e = (Element) nodes.item(i);
            out.add("[" + SoapHttp.text(e, "Code") + "] " + SoapHttp.text(e, "Msg"));
        }
        return out;
    }

    static String importe(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
