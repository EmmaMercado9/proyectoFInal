package com.ema.practica.infrastructure.moduloProduct.product.rest.model;

import com.ema.practica.infrastructure.images.ImageModel;
import com.ema.practica.infrastructure.images.ImagenModelDto;
import com.ema.practica.infrastructure.moduloProduct.productVariant.rest.model.VarianteResponseDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.*;

// ProductResponseDTO.java — detalle completo
@Data
public class ProductResponseDTO {
    private UUID idProduct;
    private String codigoBarra;
    private String sku;
    private String nameProduct;
    private String description;
    private String marca;
    private String modelo;
    private BigDecimal precioVenta;
    private BigDecimal precioPro;
    private BigDecimal costo;
    private Integer margen;
    private Boolean tieneVariantes;
    private String status;
    private Integer stockTotal;
    private Boolean stockBajo;
    private Long subCategoriaId;
    private String subCategoriaNombre;
    private List<ImagenModelDto> imageModelSetDto = new ArrayList<>();
    private List<VarianteResponseDTO> variantes;

}