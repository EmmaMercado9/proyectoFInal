package com.ema.practica.infrastructure.images.services;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.channels.MulticastChannel;
import java.util.Map;

public interface CloudDinaryService {

    Map upload(MultipartFile multipartFile) throws IOException;
    Map delete(String id) throws IOException;

}
