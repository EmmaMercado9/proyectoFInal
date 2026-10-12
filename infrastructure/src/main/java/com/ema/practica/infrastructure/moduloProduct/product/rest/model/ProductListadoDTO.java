package com.ema.practica.infrastructure.moduloProduct.product.rest.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

// ProductListadoDTO.java — para tablas, más liviano
@Data
public class ProductListadoDTO {
    private UUID idProduct;
    private String nameProduct;
    private String sku;
    private String marca;
    private BigDecimal precioVenta;
    private Integer stockTotal;
    private Boolean tieneVariantes;
    private Integer cantidadVariantes;
    private String status;
    private Boolean stockBajo;
    private String  imagenPrincipalUrl;  // ← agregar

}
