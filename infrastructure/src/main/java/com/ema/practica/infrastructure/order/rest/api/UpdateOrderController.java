package com.ema.practica.infrastructure.order.rest.api;


import com.ema.practica.infrastructure.order.entity.OrderEntity;
import com.ema.practica.infrastructure.order.rest.model.OrderDTO;
import com.ema.practica.infrastructure.order.useCases.UpdateOrderUseCaseImp;
import com.ema.practica.infrastructure.product.rest.model.ProductDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/updateOrder")
@CrossOrigin("*")

public class UpdateOrderController {

    private final UpdateOrderUseCaseImp updateOrderUseCaseImp;
    @Autowired
    public UpdateOrderController(UpdateOrderUseCaseImp updateOrderUseCaseImp){
        this.updateOrderUseCaseImp=updateOrderUseCaseImp;
    }

    @PutMapping
    public ResponseEntity<?> modifyUser(@RequestBody OrderDTO orderDTO) {
        OrderEntity order=orderDTO.dtoToEntity();
        boolean result = updateOrderUseCaseImp.update(
                order,order.getIdOrder());

        return ResponseEntity.ok().body(result);
    }
}
