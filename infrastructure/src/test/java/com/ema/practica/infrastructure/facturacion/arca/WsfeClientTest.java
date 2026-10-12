package com.ema.practica.infrastructure.facturacion.arca;

import com.ema.practica.infrastructure.facturacion.config.ArcaProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WsfeClientTest {

    @Mock WsaaClient wsaa;
    @Mock SoapHttp soap;
    ArcaProperties props = new ArcaProperties();
    WsfeClient client;

    @BeforeEach
    void setUp() {
        props.setCuit(20111111112L);
        client = new WsfeClient(props, wsaa, soap);
    }

    @Test
    void body_facturaC_respetaOrdenDelWsdlYNetoIgualTotal() {
        when(wsaa.ticket()).thenReturn(new WsaaClient.Ticket("TOK", "SIG<&>", OffsetDateTime.now().plusHours(1)));

        String xml = client.body(new WsfeClient.SolicitudCae(11, 3, 25, LocalDate.of(2026, 10, 12),
                99, 0, new BigDecimal("18000"), 5, null));

        assertThat(xml).contains("<Token>TOK</Token><Sign>SIG&lt;&amp;&gt;</Sign><Cuit>20111111112</Cuit>");
        assertThat(xml).contains("<PtoVta>3</PtoVta><CbteTipo>11</CbteTipo>");
        assertThat(xml).contains("<CbteDesde>25</CbteDesde><CbteHasta>25</CbteHasta><CbteFch>20261012</CbteFch>");
        assertThat(xml).contains("<ImpTotal>18000.00</ImpTotal><ImpTotConc>0</ImpTotConc><ImpNeto>18000.00</ImpNeto>");
        assertThat(xml).contains("<ImpIVA>0</ImpIVA><MonId>PES</MonId><MonCotiz>1</MonCotiz>"
                + "<CondicionIVAReceptorId>5</CondicionIVAReceptorId>");
        assertThat(xml).doesNotContain("CbtesAsoc").doesNotContain("<Iva>");
    }

    @Test
    void body_notaCredito_incluyeComprobanteAsociado() {
        when(wsaa.ticket()).thenReturn(new WsaaClient.Ticket("T", "S", OffsetDateTime.now().plusHours(1)));

        String xml = client.body(new WsfeClient.SolicitudCae(13, 3, 4, LocalDate.of(2026, 10, 13),
                96, 30123456, new BigDecimal("9000.5"), 5,
                new WsfeClient.CbteAsociado(11, 3, 25, 20111111112L, LocalDate.of(2026, 10, 12))));

        assertThat(xml).contains("<ImpTotal>9000.50</ImpTotal>");
        assertThat(xml).contains("<CondicionIVAReceptorId>5</CondicionIVAReceptorId><CbtesAsoc><CbteAsoc>"
                + "<Tipo>11</Tipo><PtoVta>3</PtoVta><Nro>25</Nro><Cuit>20111111112</Cuit>"
                + "<CbteFch>20261012</CbteFch></CbteAsoc></CbtesAsoc>");
    }

    @Test
    void parsear_aprobado() {
        var res = WsfeClient.parsearResultado(SoapHttp.parse(respuesta("A",
                "<CAE>76123456789012</CAE><CAEFchVto>20261022</CAEFchVto>", "")));

        assertThat(res.aprobado()).isTrue();
        assertThat(res.cae()).isEqualTo("76123456789012");
        assertThat(res.caeVencimiento()).isEqualTo(LocalDate.of(2026, 10, 22));
    }

    @Test
    void parsear_rechazado_juntaObservacionesYErrores() {
        var res = WsfeClient.parsearResultado(SoapHttp.parse(respuesta("R",
                "<Observaciones><Obs><Code>10016</Code><Msg>Numero mal</Msg></Obs></Observaciones><CAE></CAE>",
                "<Errors><Err><Code>600</Code><Msg>No autorizado</Msg></Err></Errors>")));

        assertThat(res.aprobado()).isFalse();
        assertThat(res.cae()).isNull();
        assertThat(res.observaciones()).containsExactly("[10016] Numero mal");
        assertThat(res.errores()).containsExactly("[600] No autorizado");
    }

    private static String respuesta(String resultado, String det, String errores) {
        return "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body>"
                + "<FECAESolicitarResponse xmlns=\"http://ar.gov.afip.dif.FEV1/\"><FECAESolicitarResult>"
                + "<FeCabResp><Resultado>" + resultado + "</Resultado></FeCabResp>"
                + "<FeDetResp><FECAEDetResponse><Resultado>" + resultado + "</Resultado>" + det
                + "</FECAEDetResponse></FeDetResp>" + errores
                + "</FECAESolicitarResult></FECAESolicitarResponse></soap:Body></soap:Envelope>";
    }
}
