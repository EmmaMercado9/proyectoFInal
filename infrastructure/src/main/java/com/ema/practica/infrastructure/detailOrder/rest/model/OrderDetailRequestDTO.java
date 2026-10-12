package com.ema.practica.infrastructure.detailOrder.rest.model;

import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Data
public class OrderDetailRequestDTO {

    @NotNull
    private Long variantId;                // ID de ProductVariantEntity

    @NotNull
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer quantity;
}