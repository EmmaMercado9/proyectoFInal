package com.ema.practica.infrastructure.images.services;

import com.ema.practica.infrastructure.images.ImageModel;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImagenService {
    ImageModel uploadImage(MultipartFile multipartFile) throws IOException;
    void deleteImage(ImageModel imageModel)throws IOException;
}
