package com.ema.practica.infrastructure.home.rest.model;

import com.ema.practica.core.cart.model.Cart;
import com.ema.practica.core.home.model.Home;
import com.ema.practica.core.product.model.Product;
import com.ema.practica.core.user.model.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@Data

public class HomeDTO {


    @JsonProperty(value="idHome")
    private Long idHome;

    @JsonProperty(value="street")

    private String street;
    @JsonProperty(value="city")

    private String  city;
    @JsonProperty(value="Location")

    private String  Location;
    @JsonProperty(value="nro")

    private int nro;
    @JsonProperty(value="postcode")

    private int postcode;

    public Home dtoToCore() {
        return Home.getInstance(idHome,street,city,Location,nro,postcode);
    }

    // Método para convertir de Core a DTO
    public  static HomeDTO coreToDTO(Home home) {
        return new HomeDTO(
                home.getIdHome(),
                home.getStreet(),
                home.getCity(),
                home.getLocation(),
                home.getNro(),
                home.getPostcode()
        );
    }


}
