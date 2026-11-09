package com.ema.practica.infrastructure.importacion.useCases;

import com.ema.practica.infrastructure.exception.VentaInvalidaException;
import com.ema.practica.infrastructure.importacion.crud.FacturaImportadaRepository;
import com.ema.practica.infrastructure.importacion.entity.FacturaImportadaEntity;
import com.ema.practica.infrastructure.importacion.lectores.FacturaLeida;
import com.ema.practica.infrastructure.importacion.lectores.LectorFactura;
import com.ema.practica.infrastructure.importacion.lectores.LineaFactura;
import com.ema.practica.infrastructure.importacion.rest.model.ImportacionFacturaDTO;
import com.ema.practica.infrastructure.importacion.rest.model.ModeloImportadoDTO;
import com.ema.practica.infrastructure.importacion.rest.model.ResultadoImportacionDTO;
import com.ema.practica.infrastructure.moduloProduct.atributos.crud.AtributoRepository;
import com.ema.practica.infrastructure.moduloProduct.atributos.entity.AtributoEntity;
import com.ema.practica.infrastructure.moduloProduct.atributos.entity.VarianteAtributoEntity;
import com.ema.practica.infrastructure.moduloProduct.product.crud.ProductRepository;
import com.ema.practica.infrastructure.moduloProduct.product.entity.ProductEntity;
import com.ema.practica.infrastructure.moduloProduct.product.rest.model.ProductRequestDTO;
import com.ema.practica.infrastructure.moduloProduct.product.rest.model.ProductResponseDTO;
import com.ema.practica.infrastructure.moduloProduct.product.useCases.ProductService;
import com.ema.practica.infrastructure.moduloProduct.productVariant.crud.ProductVariantRepository;
import com.ema.practica.infrastructure.moduloProduct.productVariant.entity.ProductVariantEntity;
import com.ema.practica.infrastructure.moduloProduct.productVariant.rest.model.VarianteRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ImportacionFacturaServiceTest {

    @Mock ProductRepository productRepository;
    @Mock ProductVariantRepository variantRepository;
    @Mock AtributoRepository atributoRepository;
    @Mock ProductService productService;
    @Mock FacturaImportadaRepository facturaImportadaRepository;
    @Mock LectorFactura lector;

    ImportacionFacturaService service;
    final AtributoEntity talle = new AtributoEntity(1L, "Talle");

    @BeforeEach
    void setUp() {
        service = new ImportacionFacturaService(List.of(lector), productRepository, variantRepository,
                atributoRepository, productService, facturaImportadaRepository);
        when(atributoRepository.findByNombreIgnoreCase("Talle")).thenReturn(Optional.of(talle));
        when(productRepository.findBySku(anyString())).thenReturn(Optional.empty());
        when(facturaImportadaRepository.findByCuitAndNumero(anyString(), anyString())).thenReturn(Optional.empty());
    }

    private static LineaFactura linea(String codigo, String talle, int cantidad, String costo, String lista) {
        BigDecimal c = new BigDecimal(costo);
        return new LineaFactura(codigo, "Zapatillas Runfalcon 6", "Adidas", talle, codigo + "-" + talle, cantidad,
                c, lista != null ? new BigDecimal(lista) : null, c.multiply(BigDecimal.valueOf(cantidad)));
    }

    private static FacturaLeida factura(String total, LineaFactura... lineas) {
        return new FacturaLeida("Adidas Argentina S.A.", "30-68514022-1", "0258-08518823",
                LocalDate.of(2026, 10, 10), new BigDecimal(total), List.of(lineas));
    }

    private static ProductEntity productoConTalle(String sku, String valorTalle, AtributoEntity atributo, int stock) {
        ProductEntity p = new ProductEntity();
        p.setIdProduct(UUID.randomUUID());
        p.setSku(sku);
        p.setNameProduct("Runfalcon");
        p.setPrecioVenta(new BigDecimal("119999"));
        p.setTieneVariantes(true);
        ProductVariantEntity v = new ProductVariantEntity();
        v.setVariantId(10L);
        v.setSku(sku + "-" + valorTalle);
        v.setEsDefault(false);
        v.setStock(stock);
        v.getAtributos().add(new VarianteAtributoEntity(v, atributo, valorTalle));
        p.agregarVariante(v);
        return p;
    }

    @Test
    void vistaPrevia_agrupaPorModeloYSumaTallesRepetidos() {
        ImportacionFacturaDTO dto = service.vistaPrevia(factura("335997.20",
                linea("IH9536", "39", 1, "83999.30", "119999"),
                linea("IH9536", "38.5", 2, "83999.30", "119999"),
                linea("IH9536", "39", 1, "83999.30", "119999")));

        assertThat(dto.isTotalCoincide()).isTrue();
        assertThat(dto.getUnidades()).isEqualTo(4);
        assertThat(dto.getModelos()).hasSize(1);
        ModeloImportadoDTO m = dto.getModelos().get(0);
        assertThat(m.getPrecioVenta()).isEqualByComparingTo("119999");           // precio de lista
        assertThat(m.getTalles()).extracting("talle").containsExactly("38.5", "39");  // ordenados
        assertThat(m.getTalles().get(1).getCantidad()).isEqualTo(2);              // 39: 1 + 1
    }

    @Test
    void vistaPrevia_avisaSiElTotalNoCoincide() {
        ImportacionFacturaDTO dto = service.vistaPrevia(factura("100000",
                linea("IH9536", "39", 1, "83999.30", "119999")));

        assertThat(dto.isTotalCoincide()).isFalse();
    }

    @Test
    void vistaPrevia_marcaProductosYTallesExistentes() {
        when(productRepository.findBySku("IH9536")).thenReturn(Optional.of(productoConTalle("IH9536", "39", talle, 3)));

        ModeloImportadoDTO m = service.vistaPrevia(factura("167998.60",
                linea("IH9536", "39", 1, "83999.30", "119999"),
                linea("IH9536", "40", 1, "83999.30", "119999"))).getModelos().get(0);

        assertThat(m.getProductoId()).isNotNull();
        assertThat(m.getTalles().get(0).getVarianteId()).isEqualTo(10L);   // 39 ya existe
        assertThat(m.getTalles().get(0).getStockActual()).isEqualTo(3);
        assertThat(m.getTalles().get(1).getVarianteId()).isNull();         // 40 es nuevo
    }

    @Test
    void vistaPrevia_avisaSiLaFacturaYaSeImporto() {
        FacturaImportadaEntity previa = new FacturaImportadaEntity();
        previa.setImportadaEn(LocalDateTime.of(2026, 10, 12, 10, 0));
        when(facturaImportadaRepository.findByCuitAndNumero("30-68514022-1", "0258-08518823"))
                .thenReturn(Optional.of(previa));

        ImportacionFacturaDTO dto = service.vistaPrevia(factura("83999.30", linea("IH9536", "39", 1, "83999.30", "119999")));

        assertThat(dto.getYaImportada()).contains("12/10/2026");
    }

    @Test
    void vistaPrevia_asociaPorNombreAunqueElCodigoSeaDistinto() {
        ProductEntity propio = productoConTalle("MIO-1", "39", talle, 3);
        propio.setNameProduct("zapatillas RUNFALCON 6");
        when(productRepository.findAll()).thenReturn(List.of(propio));

        ModeloImportadoDTO m = service.vistaPrevia(factura("83999.30",
                linea("OTRO-COD", "39", 1, "83999.30", null))).getModelos().get(0);

        assertThat(m.getProductoId()).isEqualTo(propio.getIdProduct());
        assertThat(m.getCoincidencia()).isEqualTo("NOMBRE");
        assertThat(m.getTalles().get(0).getVarianteId()).isEqualTo(10L);
    }

    @Test
    void vistaPrevia_nombreParcial_soloQuedaComoSugerencia() {
        ProductEntity propio = productoConTalle("MIO-1", "39", talle, 3);   // "Runfalcon"
        when(productRepository.findAll()).thenReturn(List.of(propio));

        ModeloImportadoDTO m = service.vistaPrevia(factura("83999.30",
                linea("OTRO-COD", "39", 1, "83999.30", null))).getModelos().get(0);

        assertThat(m.getProductoId()).isNull();
        assertThat(m.getCandidatos()).extracting("id").containsExactly(propio.getIdProduct());
    }

    @Test
    void confirmar_conProductoAsociado_sumaStockSinCrearOtro() throws Exception {
        ProductEntity propio = productoConTalle("MIO-1", "39", talle, 3);
        when(productRepository.findById(propio.getIdProduct())).thenReturn(Optional.of(propio));
        ImportacionFacturaDTO dto = service.vistaPrevia(factura("83999.30", linea("OTRO-COD", "39", 1, "83999.30", null)));
        dto.getModelos().get(0).setProductoId(propio.getIdProduct());

        service.confirmar(dto, "admin");

        verify(productService).sumarStockCompra(10L, 1, new BigDecimal("83999.30"));
        verify(productService, never()).crearProducto(any(), any());
    }

    @Test
    void confirmar_productoSinTalles_sumaEnSuVarianteInterna() throws Exception {
        ProductEntity propio = new ProductEntity();
        propio.setIdProduct(UUID.randomUUID());
        propio.setTieneVariantes(false);
        ProductVariantEntity interna = new ProductVariantEntity();
        interna.setVariantId(20L);
        interna.setEsDefault(true);
        propio.agregarVariante(interna);
        when(productRepository.findById(propio.getIdProduct())).thenReturn(Optional.of(propio));
        ImportacionFacturaDTO dto = service.vistaPrevia(factura("500", linea("MEDIAS", "Único", 2, "250", null)));
        dto.getModelos().get(0).setProductoId(propio.getIdProduct());

        service.confirmar(dto, "admin");

        verify(productService).sumarStockCompra(20L, 2, new BigDecimal("250"));
    }

    @Test
    void precioSugerido_sinPrecioDeLista_esCostoMas50PorCientoRedondeado() {
        assertThat(ImportacionFacturaService.precioSugerido(linea("KJ8770", "US 5", 1, "93499", null)))
                .isEqualByComparingTo("140300");   // 140.248,50 → redondeado hacia arriba a $100
    }

    @Test
    void confirmar_creaProductoYVariantesNuevos() throws Exception {
        ProductEntity creado = new ProductEntity();
        creado.setIdProduct(UUID.randomUUID());
        creado.setTieneVariantes(true);
        ProductResponseDTO resp = new ProductResponseDTO();
        resp.setIdProduct(creado.getIdProduct());
        doReturn(resp).when(productService).crearProducto(any(), any());
        when(productRepository.findById(creado.getIdProduct())).thenReturn(Optional.of(creado));

        ImportacionFacturaDTO dto = service.vistaPrevia(factura("251997.90",
                linea("IH9536", "38.5", 2, "83999.30", "119999"),
                linea("IH9536", "39", 1, "83999.30", "119999")));
        ResultadoImportacionDTO r = service.confirmar(dto, "admin");

        assertThat(r.productosCreados()).isEqualTo(1);
        assertThat(r.variantesCreadas()).isEqualTo(2);
        assertThat(r.unidades()).isEqualTo(3);

        ArgumentCaptor<ProductRequestDTO> prod = ArgumentCaptor.forClass(ProductRequestDTO.class);
        verify(productService).crearProducto(prod.capture(), isNull());
        assertThat(prod.getValue().getSku()).isEqualTo("IH9536");
        assertThat(prod.getValue().getTieneVariantes()).isTrue();

        ArgumentCaptor<VarianteRequestDTO> var = ArgumentCaptor.forClass(VarianteRequestDTO.class);
        verify(productService, times(2)).agregarVariante(eq(creado.getIdProduct()), var.capture(), isNull());
        assertThat(var.getAllValues().get(0).getStock()).isEqualTo(2);
        assertThat(var.getAllValues().get(0).getAtributos().get(0).getValor()).isEqualTo("38.5");
        verify(facturaImportadaRepository).save(any(FacturaImportadaEntity.class));
    }

    @Test
    void confirmar_sumaStockEnLaVarianteExistente() throws Exception {
        ProductEntity existente = productoConTalle("IH9536", "39", talle, 3);
        when(productRepository.findBySku("IH9536")).thenReturn(Optional.of(existente));
        when(productRepository.findById(existente.getIdProduct())).thenReturn(Optional.of(existente));

        ImportacionFacturaDTO dto = service.vistaPrevia(factura("167998.60", linea("IH9536", "39", 2, "83999.30", "119999")));
        ResultadoImportacionDTO r = service.confirmar(dto, "admin");

        assertThat(r.variantesConStockSumado()).isEqualTo(1);
        verify(productService).sumarStockCompra(10L, 2, new BigDecimal("83999.30"));
        verify(productService, never()).crearProducto(any(), any());
    }

    @Test
    void confirmar_rechazaUnaFacturaYaImportada() {
        ImportacionFacturaDTO dto = service.vistaPrevia(factura("83999.30", linea("IH9536", "39", 1, "83999.30", "119999")));
        FacturaImportadaEntity previa = new FacturaImportadaEntity();
        previa.setImportadaEn(LocalDateTime.now());
        when(facturaImportadaRepository.findByCuitAndNumero("30-68514022-1", "0258-08518823"))
                .thenReturn(Optional.of(previa));

        assertThatThrownBy(() -> service.confirmar(dto, "admin"))
                .isInstanceOf(VentaInvalidaException.class)
                .hasMessageContaining("ya se importó");
        verify(facturaImportadaRepository, never()).save(any());
    }

    @Test
    void confirmar_rechazaTallesRepetidosAlCorregirLaVistaPrevia() {
        ImportacionFacturaDTO dto = service.vistaPrevia(factura("167998.60",
                linea("IH9536", "39", 1, "83999.30", "119999"),
                linea("IH9536", "40", 1, "83999.30", "119999")));
        dto.getModelos().get(0).getTalles().get(1).setTalle("39");

        assertThatThrownBy(() -> service.confirmar(dto, "admin"))
                .isInstanceOf(VentaInvalidaException.class)
                .hasMessageContaining("repetido");
    }
}
