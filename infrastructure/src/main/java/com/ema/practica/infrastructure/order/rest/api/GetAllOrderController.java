package com.ema.practica.infrastructure.order.rest.api;

import com.ema.practica.core.cart.input.IConsultCartInput;
import com.ema.practica.core.cart.model.Cart;
import com.ema.practica.core.order.input.IConsultOrderInput;
import com.ema.practica.core.order.model.Order;
import com.ema.practica.infrastructure.order.entity.OrderEntity;
import com.ema.practica.infrastructure.order.rest.model.OrderDTO;
import com.ema.practica.infrastructure.order.useCases.ConsutlOrderUseCaseImp;
import org.springframework.data.domain.jaxb.SpringDataJaxb;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/getAllOrders")
@CrossOrigin("*")

public class GetAllOrderController {
    private final IConsultOrderInput IConsultOrderInput;
    private  final ConsutlOrderUseCaseImp consutlOrderUseCaseImp;

    public GetAllOrderController(IConsultOrderInput IConsultOrderInput, ConsutlOrderUseCaseImp consutlOrderUseCaseImp){
        this.IConsultOrderInput=IConsultOrderInput;
        this.consutlOrderUseCaseImp = consutlOrderUseCaseImp;
    }

    @GetMapping()

    public ResponseEntity<List<OrderDTO>> GetAllOrders(){
        Collection<Order> list=IConsultOrderInput.getOrders();

        List<OrderDTO> list1=list.stream().map(OrderDTO::coreToDTO).toList();

        if (!list1.isEmpty()) {
            return ResponseEntity.ok(list1);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @GetMapping("/Order/user")
    public ResponseEntity<List<OrderDTO>> getByUser(){
        UserDetails userDetails=(UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username=userDetails.getUsername();
        return new ResponseEntity<>(this.consutlOrderUseCaseImp.getOrderByUser(username).stream().map(OrderDTO::coreToDTO).toList(), HttpStatus.OK);
    }
}
