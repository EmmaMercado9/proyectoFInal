package com.ema.practica.infrastructure.order.rest.api;

import com.ema.practica.infrastructure.order.rest.model.OrderRequest;
import com.ema.practica.infrastructure.order.useCases.CreateOrderUseCaseImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin("*")

public class CreateOrderController {

    private final CreateOrderUseCaseImp createOrderUseCaseImp;
    @Autowired
    public CreateOrderController(CreateOrderUseCaseImp createOrderUseCaseImp){
        this.createOrderUseCaseImp = createOrderUseCaseImp;
    }

    @PostMapping("/createOrder")
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest orderRequest){
        this.createOrderUseCaseImp.createOrder(orderRequest);
        Map<String,String> response=new HashMap<String,String>();
        response.put("Message","Orden Exitosa");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
