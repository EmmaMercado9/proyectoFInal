package com.ema.practica.infrastructure.moduloProduct.product.rest.mapper;

import com.ema.practica.infrastructure.images.ImageModel;
import com.ema.practica.infrastructure.images.ImagenModelDto;
import com.ema.practica.infrastructure.moduloProduct.atributos.entity.VarianteAtributoEntity;
import com.ema.practica.infrastructure.moduloProduct.product.entity.ProductEntity;
import com.ema.practica.infrastructure.moduloProduct.product.rest.model.ProductListadoDTO;
import com.ema.practica.infrastructure.moduloProduct.product.rest.model.ProductResponseDTO;
import com.ema.practica.infrastructure.moduloProduct.productVariant.entity.ProductVariantEntity;
import com.ema.practica.infrastructure.moduloProduct.productVariant.rest.model.VarianteResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// ProductMapper.java
@Component
public class ProductMapper {

    // ─── Entity → DTO ─────────────────────────────────────────────────────

    public ProductResponseDTO toResponseDTO(ProductEntity product) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setIdProduct(product.getIdProduct());
        dto.setCodigoBarra(product.getCodigoBarra());
        dto.setSku(product.getSku());
        dto.setNameProduct(product.getNameProduct());
        dto.setDescription(product.getDescription());
        dto.setMarca(product.getMarca());
        dto.setModelo(product.getModelo());
        dto.setPrecioVenta(product.getPrecioVenta());
        dto.setPrecioPro(product.getPrecioPro());
        dto.setCosto(product.getCosto());
        dto.setMargen(product.getMargen());
        dto.setTieneVariantes(product.getTieneVariantes());
        dto.setStatus(product.getStatus().name());
        dto.setStockTotal(product.getStockTotal());
        dto.setStockBajo(product.tieneStockBajo());

        if (product.getSubCategoria() != null) {
            dto.setSubCategoriaId(product.getSubCategoria().getSubId());
            dto.setSubCategoriaNombre(product.getSubCategoria().getNombre());
        }

        List<VarianteResponseDTO> variantes = product.getVariants().stream()
                .map(this::toVarianteDTO)
                .toList();
        dto.setVariantes(variantes);

        List<ImagenModelDto> imagenModelDtos=product.getImageModelSet().stream().map(this::toImageDTO).toList();
        dto.setImageModelSetDto(imagenModelDtos);
        return dto;
    }

    public ProductListadoDTO toListadoDTO(ProductEntity product) {
        ProductListadoDTO dto = new ProductListadoDTO();
        dto.setIdProduct(product.getIdProduct());
        dto.setNameProduct(product.getNameProduct());
        dto.setSku(product.getSku());
        dto.setMarca(product.getMarca());
        dto.setPrecioVenta(product.getPrecioVenta());
        dto.setStockTotal(product.getStockTotal());
        dto.setTieneVariantes(product.getTieneVariantes());
        dto.setStatus(product.getStatus().name());
        dto.setStockBajo(product.tieneStockBajo());

        // Variantes sin contar la default
        int cantVariantes = (int) product.getVariants().stream()
                .filter(v -> !Boolean.TRUE.equals(v.getEsDefault()))
                .count();
        dto.setCantidadVariantes(cantVariantes);

        // ── Imagen principal ──────────────────────────────────────────────────
        String imagenUrl = null;
        if (!Boolean.TRUE.equals(product.getTieneVariantes())) {
            // Sin variantes → imagen del producto
            imagenUrl = product.getImageModelSet().stream()
                    .filter(img -> Boolean.TRUE.equals(img.getEsPrincipal()))
                    .findFirst()
                    .map(ImageModel::getUrl)
                    .orElse(
                            product.getImageModelSet().isEmpty() ? null
                                    : product.getImageModelSet().iterator().next().getUrl()
                    );
        } else {
            // Con variantes → primera imagen principal de cualquier variante
            imagenUrl = product.getVariants().stream()
                    .flatMap(v -> v.getImageModelSet().stream())
                    .filter(img -> Boolean.TRUE.equals(img.getEsPrincipal()))
                    .findFirst()
                    .map(ImageModel::getUrl)
                    .orElse(
                            product.getVariants().stream()
                                    .flatMap(v -> v.getImageModelSet().stream())
                                    .findFirst()
                                    .map(ImageModel::getUrl)
                                    .orElse(null)
                    );
        }
        dto.setImagenPrincipalUrl(imagenUrl);

        return dto;
    }

    private ImagenModelDto toImageDTO(ImageModel image) {

        ImagenModelDto dto = new ImagenModelDto();

        dto.setId(image.getId());
        dto.setName(image.getName());
        dto.setUrl(image.getUrl());

        return dto;
    }

    public VarianteResponseDTO toVarianteDTO(ProductVariantEntity variante) {
        VarianteResponseDTO dto = new VarianteResponseDTO();
        dto.setVariantId(variante.getVariantId());
        dto.setSku(variante.getSku());
        dto.setCodigoBarra(variante.getCodigoBarra());
        dto.setPrecioVenta(variante.getPrecioEfectivo());
        dto.setPrecioPro(variante.getPrecioPro());
        dto.setCosto(variante.getCosto());
        dto.setStock(variante.getStock());
        dto.setStockMinimo(variante.getStockMinimo());
        dto.setEsDefault(variante.getEsDefault());
        dto.setStatus(variante.getStatus().name());

        Map<String, String> atributos = variante.getAtributos().stream()
                .collect(Collectors.toMap(
                        a -> a.getAtributo().getNombre(),
                        VarianteAtributoEntity::getValor));
        dto.setAtributos(atributos);
        dto.setDescripcion(variante.getDescripcionAtributos());

        List<ImagenModelDto> imagenModelDtos=variante.getImageModelSet().stream().map(this::toImageDTO).toList();
        dto.setImageModelSetDto(imagenModelDtos);
        return dto;
    }
}