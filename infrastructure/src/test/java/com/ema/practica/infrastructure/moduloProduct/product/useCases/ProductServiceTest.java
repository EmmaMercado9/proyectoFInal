package com.ema.practica.infrastructure.moduloProduct.product.useCases;

import com.ema.practica.infrastructure.detailOrder.crud.OrderDetailRepository;
import com.ema.practica.infrastructure.exception.RecursoNoEncontradoException;
import com.ema.practica.infrastructure.exception.VentaInvalidaException;
import com.ema.practica.infrastructure.images.services.ImagenService;
import com.ema.practica.infrastructure.moduloProduct.atributos.crud.AtributoRepository;
import com.ema.practica.infrastructure.moduloProduct.atributos.crud.VarianteAtributoRepository;
import com.ema.practica.infrastructure.moduloProduct.product.crud.ProductRepository;
import com.ema.practica.infrastructure.moduloProduct.product.entity.ProductEntity;
import com.ema.practica.infrastructure.moduloProduct.product.rest.mapper.ProductMapper;
import com.ema.practica.infrastructure.moduloProduct.product.rest.model.ProductUpdateDTO;
import com.ema.practica.infrastructure.moduloProduct.productVariant.crud.ProductVariantRepository;
import com.ema.practica.infrastructure.moduloProduct.productVariant.entity.ProductVariantEntity;
import com.ema.practica.infrastructure.moduloProduct.productVariant.rest.model.VarianteUpdateDTO;
import com.ema.practica.infrastructure.moduloProduct.subcategoria.crud.SubCategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.ema.practica.infrastructure.util.status.StatusEntity;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock ProductRepository productRepository;
    @Mock ProductVariantRepository variantRepository;
    @Mock VarianteAtributoRepository varianteAtributoRepository;
    @Mock AtributoRepository atributoRepository;
    @Mock SubCategoriaRepository subCategoriaRepository;
    @Mock ProductMapper productMapper;
    @Mock ImagenService imagenService;
    @Mock OrderDetailRepository orderDetailRepository;

    @InjectMocks ProductService productService;

    private final UUID id = UUID.randomUUID();
    private ProductEntity producto;
    private ProductVariantEntity varianteDefault;

    @BeforeEach
    void setUp() {
        producto = new ProductEntity();
        producto.setIdProduct(id);
        producto.setNameProduct("Viejo");
        producto.setSku("OLD");
        producto.setPrecioVenta(new BigDecimal("100"));

        varianteDefault = new ProductVariantEntity();
        varianteDefault.setVariantId(1L);
        varianteDefault.setSku("OLD-DEFAULT");
        varianteDefault.setEsDefault(true);
        producto.agregarVariante(varianteDefault);
    }

    private ProductUpdateDTO update(String sku) {
        ProductUpdateDTO dto = new ProductUpdateDTO();
        dto.setNameProduct("Nuevo");
        dto.setSku(sku);
        dto.setCodigoBarra("   ");
        dto.setPrecioVenta(new BigDecimal("200"));
        dto.setCosto(new BigDecimal("150"));
        return dto;
    }

    @Test
    void actualizarProducto_cambiaDatosMargenYSkuDefault() {
        when(productRepository.findById(id)).thenReturn(Optional.of(producto));
        when(productRepository.existsBySkuAndIdProductNot("NEW", id)).thenReturn(false);

        productService.actualizarProducto(id, update("NEW"));

        assertThat(producto.getNameProduct()).isEqualTo("Nuevo");
        assertThat(producto.getPrecioVenta()).isEqualByComparingTo("200");
        assertThat(producto.getMargen()).isEqualTo(25);          // (200-150)/200
        assertThat(producto.getCodigoBarra()).isNull();          // "   " → null
        assertThat(varianteDefault.getSku()).isEqualTo("NEW-DEFAULT");
    }

    @Test
    void actualizarProducto_skuDeOtroProducto_falla() {
        when(productRepository.findById(id)).thenReturn(Optional.of(producto));
        when(productRepository.existsBySkuAndIdProductNot("DUP", id)).thenReturn(true);

        assertThatThrownBy(() -> productService.actualizarProducto(id, update("DUP")))
                .isInstanceOf(VentaInvalidaException.class);
        assertThat(producto.getNameProduct()).isEqualTo("Viejo");
    }

    @Test
    void actualizarProducto_inexistente_falla() {
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.actualizarProducto(id, update("X")))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void actualizarVariante_cambiaPrecioYStockMinimo() {
        ProductVariantEntity v = new ProductVariantEntity();
        v.setVariantId(2L);
        v.setEsDefault(false);
        producto.agregarVariante(v);
        when(variantRepository.findById(2L)).thenReturn(Optional.of(v));

        VarianteUpdateDTO dto = new VarianteUpdateDTO();
        dto.setSku("V-42");
        dto.setPrecioVenta(new BigDecimal("120"));
        dto.setCosto(new BigDecimal("60"));
        dto.setStockMinimo(2);

        productService.actualizarVariante(2L, dto);

        assertThat(v.getSku()).isEqualTo("V-42");
        assertThat(v.getPrecioEfectivo()).isEqualByComparingTo("120");
        assertThat(v.getMargen()).isEqualTo(50);
        assertThat(v.getStockMinimo()).isEqualTo(2);
    }

    @Test
    void actualizarVariante_default_falla() {
        when(variantRepository.findById(1L)).thenReturn(Optional.of(varianteDefault));
        VarianteUpdateDTO dto = new VarianteUpdateDTO();
        dto.setSku("X");

        assertThatThrownBy(() -> productService.actualizarVariante(1L, dto))
                .isInstanceOf(VentaInvalidaException.class);
    }

    // ── Eliminar / desactivar ───────────────────────────────────────────────

    private ProductVariantEntity variante(long id) {
        ProductVariantEntity v = new ProductVariantEntity();
        v.setVariantId(id);
        v.setSku("V" + id);
        v.setEsDefault(false);
        producto.agregarVariante(v);
        return v;
    }

    @Test
    void eliminarProducto_conVentas_falla() {
        when(productRepository.findById(id)).thenReturn(Optional.of(producto));
        when(orderDetailRepository.existsByVariant_Product_IdProduct(id)).thenReturn(true);

        assertThatThrownBy(() -> productService.eliminarProducto(id))
                .isInstanceOf(VentaInvalidaException.class)
                .hasMessageContaining("Desactivalo");
        verify(productRepository, never()).delete(any());
    }

    @Test
    void eliminarProducto_sinVentas_borra() {
        when(productRepository.findById(id)).thenReturn(Optional.of(producto));

        productService.eliminarProducto(id);

        verify(productRepository).delete(producto);
    }

    @Test
    void eliminarVariante_sinVentas_laQuitaDelProducto() {
        ProductVariantEntity v2 = variante(2);
        variante(3);
        when(variantRepository.findById(2L)).thenReturn(Optional.of(v2));

        productService.eliminarVariante(2L);

        assertThat(producto.getVariants()).doesNotContain(v2);
    }

    @Test
    void eliminarVariante_conVentas_falla() {
        ProductVariantEntity v2 = variante(2);
        variante(3);
        when(variantRepository.findById(2L)).thenReturn(Optional.of(v2));
        when(orderDetailRepository.existsByVariant_VariantId(2L)).thenReturn(true);

        assertThatThrownBy(() -> productService.eliminarVariante(2L))
                .isInstanceOf(VentaInvalidaException.class)
                .hasMessageContaining("Desactivala");
        assertThat(producto.getVariants()).contains(v2);
    }

    @Test
    void eliminarVariante_unica_falla() {
        ProductVariantEntity v2 = variante(2);
        when(variantRepository.findById(2L)).thenReturn(Optional.of(v2));

        assertThatThrownBy(() -> productService.eliminarVariante(2L))
                .isInstanceOf(VentaInvalidaException.class)
                .hasMessageContaining("única variante");
    }

    @Test
    void eliminarVariante_default_falla() {
        when(variantRepository.findById(1L)).thenReturn(Optional.of(varianteDefault));

        assertThatThrownBy(() -> productService.eliminarVariante(1L))
                .isInstanceOf(VentaInvalidaException.class)
                .hasMessageContaining("eliminá el producto");
    }

    @Test
    void desactivarVariante_noSumaStock() {
        ProductVariantEntity v2 = variante(2);
        v2.setStock(7);
        varianteDefault.setStock(0);
        when(variantRepository.findById(2L)).thenReturn(Optional.of(v2));

        productService.cambiarEstadoVariante(2L, false);

        assertThat(v2.getStatus()).isEqualTo(StatusEntity.INACTIVO);
        assertThat(producto.getStockTotal()).isZero();
    }
}
