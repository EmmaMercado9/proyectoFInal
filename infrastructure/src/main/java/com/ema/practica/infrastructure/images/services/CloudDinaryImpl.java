package com.ema.practica.infrastructure.images.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.ema.practica.infrastructure.exception.VentaInvalidaException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class CloudDinaryImpl implements CloudDinaryService  {
    private final Cloudinary cloudinary;

    public CloudDinaryImpl(@Value("${cloudinary.cloud-name:}") String cloudName,
                           @Value("${cloudinary.api-key:}") String apiKey,
                           @Value("${cloudinary.api-secret:}") String apiSecret){
        Map<String,String> valuesMap=new HashMap<>();
        valuesMap.put("cloud_name",cloudName);
        valuesMap.put("api_key",apiKey);
        valuesMap.put("api_secret",apiSecret);
        cloudinary=new Cloudinary(valuesMap);
    }


    @Override
    public Map upload(MultipartFile multipartFile) throws IOException {
        try {
            // Se sube directo desde memoria: sin archivos temporales que se pisen o queden tirados
            return cloudinary.uploader().upload(multipartFile.getBytes(), ObjectUtils.emptyMap());
        } catch (RuntimeException e) {
            // Cloudinary informa los problemas de configuración (nombre de nube, claves) como RuntimeException
            throw new VentaInvalidaException("No se pudo subir la imagen a Cloudinary: " + e.getMessage()
                    + ". Revisá las variables CLOUDINARY_* del servidor.");
        }
    }

    @Override
    public Map delete(String id) throws IOException {
    return cloudinary.uploader().destroy(id,ObjectUtils.emptyMap());
    }
}
