package com.ema.practica.infrastructure.detailOrder.rest.api;

import com.ema.practica.core.detailOrder.input.ICreateDetailOrderInput;
import com.ema.practica.core.order.model.Order;
import com.ema.practica.core.product.model.Product;
import com.ema.practica.infrastructure.detailOrder.rest.model.DetailOrderDTO;
import com.ema.practica.infrastructure.order.useCases.ConsutlOrderUseCaseImp;
import com.ema.practica.infrastructure.product.useCases.ListProductUseCaseImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/AddDetail")
@CrossOrigin("*")

public class CreateDetailController {

    ICreateDetailOrderInput iCreateDetailOrderInput;
    private final ConsutlOrderUseCaseImp consutlOrderUseCaseImp;

    private final ListProductUseCaseImp listProductUseCaseImp;
    @Autowired
    public CreateDetailController(ICreateDetailOrderInput iCreateDetailOrderInput, ConsutlOrderUseCaseImp consutlOrderUseCaseImp, ListProductUseCaseImp listProductUseCaseImp){
        this.iCreateDetailOrderInput=iCreateDetailOrderInput;
        this.consutlOrderUseCaseImp = consutlOrderUseCaseImp;
        this.listProductUseCaseImp = listProductUseCaseImp;
    }

    @PostMapping()
    public ResponseEntity<?> addProductCart(@RequestBody DetailOrderDTO detailOrderDTO){
        Product product =listProductUseCaseImp.getProId(detailOrderDTO.getProductId());
        boolean result= iCreateDetailOrderInput.createDetailOrder(detailOrderDTO.dtoToCore(product));
        return  ResponseEntity.ok().body(result);
    }
}
