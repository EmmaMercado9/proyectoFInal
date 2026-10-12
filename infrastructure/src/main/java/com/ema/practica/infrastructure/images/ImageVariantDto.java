package com.ema.practica.infrastructure.images;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class ImageVariantDto {
    private String  imageId;     // public_id de Cloudinary
    private String  name;
    private String  url;         // https://res.cloudinary.com/... — directo al img
    private Boolean esPrincipal;
}