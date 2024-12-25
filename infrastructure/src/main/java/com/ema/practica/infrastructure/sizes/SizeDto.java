package com.ema.practica.infrastructure.sizes;

import com.ema.practica.infrastructure.product.entity.ProductEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SizeDto {
    @JsonProperty(value="id")

    private Long id;
    @JsonProperty(value="size")

    private String size; // Ej: "38 (UK 6)"
    @JsonProperty(value="stock")

    private int stock;
    @JsonProperty(value="available")

    private boolean available;

}
