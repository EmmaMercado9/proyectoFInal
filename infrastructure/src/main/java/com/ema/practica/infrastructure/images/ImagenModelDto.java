package com.ema.practica.infrastructure.images;

import com.ema.practica.infrastructure.moduloProduct.product.entity.ProductEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImagenModelDto {

        private Long id;
        @JsonProperty(value = "name")
        private String name;

        private Boolean esPrincipal ;
        @JsonProperty(value = "url")

        private String url;
        @JsonProperty(value = "imageId")

        private String imageId;
        @JsonProperty(value = "idproduct")
        private UUID idproduct;


        public static ImagenModelDto entityToDto(ImageModel imageModel){
                return new ImagenModelDto(imageModel.getId(),imageModel.getName(),imageModel.getEsPrincipal(),imageModel.getUrl(),imageModel.getImageId(),imageModel.getProduct().getIdProduct());
        }




}
