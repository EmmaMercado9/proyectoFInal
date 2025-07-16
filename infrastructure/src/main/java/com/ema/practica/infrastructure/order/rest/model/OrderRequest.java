package com.ema.practica.infrastructure.order.rest.model;

import com.ema.practica.infrastructure.util.user.entitty.UserEntity;

import java.util.List;
import java.util.Map;

public class OrderRequest {
    private List<Map<String, Object>> listCart; // Cada elemento tiene las propiedades 'productId' y 'cantidad'
    private UserEntity user;
}
