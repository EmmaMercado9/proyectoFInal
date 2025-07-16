package com.ema.practica.infrastructure.images;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageModelCrud extends CrudRepository<ImageModel,Long> {
}
