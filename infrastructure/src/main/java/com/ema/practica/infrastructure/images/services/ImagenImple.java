package com.ema.practica.infrastructure.images.services;

import com.cloudinary.Cloudinary;
import com.ema.practica.infrastructure.images.ImageModel;
import com.ema.practica.infrastructure.images.ImageModelCrud;
import com.ema.practica.infrastructure.moduloProduct.productVariant.entity.ProductVariantEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
    public List<ImageModel> uploadImages(List<MultipartFile> multipartFile) throws IOException {
    List<ImageModel> images=new ArrayList<>();

    if(multipartFile==null || multipartFile.isEmpty()){
        return images;
    }


    for(MultipartFile file:multipartFile){
        if(file.isEmpty()){
            continue;
        }
        Map uploadResult=cloudDinaryService.upload(file);
        String imageUrl=(String) uploadResult.get("url");
        String imageId=(String) uploadResult.get("public_id");
        ImageModel imageModel=new ImageModel();
        imageModel.setImageId(imageId);
        imageModel.setUrl(imageUrl);
        imageModel.setName(file.getOriginalFilename());
        images.add(imageModel);

    }
    return images;


        }

    @Override
    public List<ImageModel> uploadImagesVariante(
            List<MultipartFile> files,
            ProductVariantEntity variante) throws IOException {

        List<ImageModel> imagenes = new ArrayList<>();
        if (files == null || files.isEmpty()) return imagenes;

        boolean primera = true;
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            Map uploadResult = cloudDinaryService.upload(file);
            String url      = (String) uploadResult.get("url");
            String publicId = (String) uploadResult.get("public_id");

            ImageModel img = new ImageModel();
            img.setImageId(publicId);
            img.setUrl(url);
            img.setName(file.getOriginalFilename());
            img.setVariant(variante);         // ← asociar a la variante
            img.setEsPrincipal(primera);       // la primera es la principal
            primera = false;

            imageModelCrud.save(img);
            imagenes.add(img);
        }
        return imagenes;
    }

    @Override
    public void borrarDeCloudinary(List<String> imageIds) {
        for (String id : imageIds) {
            if (id == null || id.isBlank()) continue;
            try {
                cloudDinaryService.delete(id);
            } catch (Exception e) {
                // No frena la operación: queda un archivo huérfano en Cloudinary
                org.slf4j.LoggerFactory.getLogger(ImagenImple.class)
                        .warn("No se pudo borrar la imagen {} de Cloudinary: {}", id, e.getMessage());
            }
        }
    }

    @Override
    public void deleteImage(ImageModel imageModel) throws IOException {
        cloudDinaryService.delete(imageModel.getImageId());
        imageModelCrud.delete(imageModel);
    }
}
