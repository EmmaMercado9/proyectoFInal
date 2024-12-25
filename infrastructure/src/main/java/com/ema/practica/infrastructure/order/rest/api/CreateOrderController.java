package com.ema.practica.infrastructure.order.rest.api;

import com.ema.practica.core.order.input.ICreateOrderInput;
import com.ema.practica.infrastructure.cart.rest.model.ProductCartDTO;
import com.ema.practica.infrastructure.order.rest.model.OrderDTO;
import com.ema.practica.infrastructure.order.rest.model.OrderRequestBody;
import com.ema.practica.infrastructure.order.useCases.CreateOrderUseCaseImp;
import com.ema.practica.infrastructure.user.rest.model.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin("*")

public class CreateOrderController {

    private final ICreateOrderInput iCreateOrderInput;
    private final CreateOrderUseCaseImp createOrderUseCaseImp;
    @Autowired
    public CreateOrderController(ICreateOrderInput iCreateOrderInput, CreateOrderUseCaseImp createOrderUseCaseImp){
        this.iCreateOrderInput=iCreateOrderInput;
        this.createOrderUseCaseImp = createOrderUseCaseImp;
    }

    @PostMapping("/createOrder")
    public ResponseEntity<?> createOrder(@RequestBody OrderRequestBody orderRequestBody){
        this.createOrderUseCaseImp.createOrder(orderRequestBody);
        Map<String,String> response=new HashMap<String,String>();
        response.put("Message","Orden Exitosa");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
