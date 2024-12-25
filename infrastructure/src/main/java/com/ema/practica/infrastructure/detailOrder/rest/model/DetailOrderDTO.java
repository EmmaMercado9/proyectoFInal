package com.ema.practica.infrastructure.detailOrder.rest.model;

import com.ema.practica.core.cart.model.Cart;
import com.ema.practica.core.detailOrder.model.DetailOrder;
import com.ema.practica.core.order.model.Order;
import com.ema.practica.core.product.model.Product;
import com.ema.practica.core.user.model.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@Data

public class DetailOrderDTO {


    @JsonProperty(value="idDetailOrder")
    private Long idDetailOrder;

    // Solo el ID del Producto, no toda la entidad
    @JsonProperty(value="productId")
    private Long productId;

    @JsonProperty(value="amount")
    private int amount;

    @JsonProperty(value="priceUnitary")
    private double priceUnitary;

    @JsonProperty(value="subtotalPrice")
    private double subtotalPrice;

    // Método para convertir de DTO a Core
    public DetailOrder dtoToCore(Product product) {
        return DetailOrder.getInstance(
                idDetailOrder,
                product,
                amount,
                priceUnitary,
                subtotalPrice
        );
    }

    // Método para convertir de Core a DTO
    public static DetailOrderDTO coreToDTO(DetailOrder detailOrder) {
        return new DetailOrderDTO(
                detailOrder.getIdDetailOrder(),
                detailOrder.getProductList().getIdProduct(),  // Solo el ID del Producto
                detailOrder.getAmount(),
                detailOrder.getPriceUnitary(),
                detailOrder.getSubtotalPrice()
        );
    }


}
