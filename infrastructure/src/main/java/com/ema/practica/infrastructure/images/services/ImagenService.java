package com.ema.practica.infrastructure.images.services;

import com.ema.practica.infrastructure.images.ImageModel;
import com.ema.practica.infrastructure.moduloProduct.productVariant.entity.ProductVariantEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ImagenService {
    List<ImageModel> uploadImages(List<MultipartFile> multipartFile) throws IOException;
    List<ImageModel> uploadImagesVariante(
            List<MultipartFile> files,
            ProductVariantEntity variante) throws IOException;
    void deleteImage(ImageModel imageModel)throws IOException;

    /** Borra archivos de Cloudinary (las filas ya las borró el cascade). Best effort. */
    void borrarDeCloudinary(List<String> imageIds);
}
