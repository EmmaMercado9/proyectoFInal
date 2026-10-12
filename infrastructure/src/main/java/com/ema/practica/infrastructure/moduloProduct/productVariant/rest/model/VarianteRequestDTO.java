package com.ema.practica.infrastructure.moduloProduct.productVariant.rest.model;

import com.ema.practica.infrastructure.moduloProduct.atributos.rest.model.AtributoValorDTO;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Data
public class VarianteRequestDTO {
    @NotBlank
    private String sku;
    private String codigoBarra;
    private BigDecimal precioVenta;
    private BigDecimal precioPro;
    private BigDecimal costo;
    @NotNull
    @Min(0) private Integer stock;
    private Integer stockMinimo;
    @NotEmpty
    @Valid
    private List<AtributoValorDTO> atributos;
}