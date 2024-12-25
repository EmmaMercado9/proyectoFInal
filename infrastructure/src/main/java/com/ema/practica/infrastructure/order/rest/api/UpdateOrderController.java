package com.ema.practica.infrastructure.order.rest.api;

import com.ema.practica.core.cart.input.IDeleteProductCartInput;
import com.ema.practica.core.cart.model.Cart;
import com.ema.practica.core.order.input.IUpdateOrderStatusInput;
import com.ema.practica.core.order.model.Order;
import com.ema.practica.core.product.model.Product;
import com.ema.practica.infrastructure.order.rest.model.OrderDTO;
import com.ema.practica.infrastructure.product.rest.model.ProductDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/updateOrder")
@CrossOrigin("*")

public class UpdateOrderController {

    IUpdateOrderStatusInput iUpdateOrderStatusInput;
    public UpdateOrderController(IUpdateOrderStatusInput iUpdateOrderStatusInput){
        this.iUpdateOrderStatusInput=iUpdateOrderStatusInput;
    }

    @PutMapping
    public ResponseEntity<?> modifyUser(@RequestBody OrderDTO orderDTO) {
        Order order=orderDTO.dtoToCore();
        boolean result = iUpdateOrderStatusInput.updateOrder(
                order.getUser(),
        order.getPayment(),
        order.getOrderDate(),
        order.getTotalAmount(),
        order.getStatus(),
        order.getIdOrder());

        return ResponseEntity.ok().body(result);
    }
}
