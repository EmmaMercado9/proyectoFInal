package com.ema.practica.infrastructure.moduloProduct.productVariant.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class VariantDTO {
    private Long variantId;  // opcional, para actualizaciones
    @JsonProperty(value = "sku")
    private String sku;
    @JsonProperty(value = "codigo_barra")
    private String codigo_barra;
    @JsonProperty(value ="price_venta")
    private BigDecimal price_venta;
    @JsonProperty(value ="price_pro")
    private BigDecimal price_pro;
    @JsonProperty(value ="costo")
    private BigDecimal costo;
    @JsonProperty(value ="margen")
    private Integer margen;
    @JsonProperty(value ="stock")
    private Integer stock;
    @JsonProperty(value ="stock_minimo")
    private Integer stock_minimo;

    @JsonProperty(value = "atributos")
    private Map<String, String> atributos; // {"Talle": "12", "color": "azul"}



    // Método para convertir de Core a DTO


}
