package com.ema.practica.infrastructure.facturacion.arca;

import com.ema.practica.infrastructure.facturacion.config.ArcaProperties;
import com.ema.practica.infrastructure.facturacion.crud.ConfiguracionFacturacionRepository;
import com.ema.practica.infrastructure.facturacion.useCases.ConfiguracionArcaService;
import org.mockito.Mockito;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

// Sonda manual contra homologación (no corre en el build normal)
class WsaaSondaManualTest {
    @Test
    @EnabledIfSystemProperty(named = "sonda.cert", matches = ".+")
    void sonda() {
        ArcaProperties p = new ArcaProperties();
        p.setCertPath(System.getProperty("sonda.cert"));
        p.setKeyPath(System.getProperty("sonda.key"));
        p.setDirectorio(System.getProperty("java.io.tmpdir") + "/sonda-arca");
        var config = new ConfiguracionArcaService(p, Mockito.mock(ConfiguracionFacturacionRepository.class));
        WsaaClient c = new WsaaClient(p, config, new SoapHttp());
        try {
            c.ticket();
            System.out.println("SONDA RESULTADO: ticket obtenido (inesperado)");
        } catch (ArcaException e) {
            System.out.println("SONDA RESULTADO: " + e.getMessage());
        }
    }
}
