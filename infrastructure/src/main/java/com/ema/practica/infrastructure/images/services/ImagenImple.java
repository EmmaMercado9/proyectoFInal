package com.ema.practica.infrastructure.images.services;

import com.cloudinary.Cloudinary;
import com.ema.practica.infrastructure.images.ImageModel;
import com.ema.practica.infrastructure.images.ImageModelCrud;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class ImagenImple implements ImagenService{
    private final CloudDinaryService cloudDinaryService;
    private final ImageModelCrud imageModelCrud;

    public ImagenImple(CloudDinaryService cloudDinaryService, ImageModelCrud imageModelCrud) {
        this.cloudDinaryService = cloudDinaryService;
        this.imageModelCrud = imageModelCrud;
    }
    @Override
    public ImageModel uploadImage(MultipartFile multipartFile) throws IOException {
    Map uploadResult =cloudDinaryService.upload(multipartFile);
    String imageUrl=(String)uploadResult.get("url");
        String imageId=(String)uploadResult.get("public_id");
        return new ImageModel(multipartFile.getOriginalFilename(),imageUrl,imageId);
    }

    @Override
    public void deleteImage(ImageModel imageModel) throws IOException {
        cloudDinaryService.delete(imageModel.getImageId());
        imageModelCrud.delete(imageModel);
    }
}
