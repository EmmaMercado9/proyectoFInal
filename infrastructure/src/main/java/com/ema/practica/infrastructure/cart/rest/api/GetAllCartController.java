package com.ema.practica.infrastructure.cart.rest.api;

import com.ema.practica.core.cart.input.IConsultCartInput;
import com.ema.practica.core.cart.model.Cart;
import com.ema.practica.core.product.input.IListProductInput;
import com.ema.practica.core.product.model.Product;
import com.ema.practica.infrastructure.cart.rest.model.ProductCartDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/getAllCart")
@CrossOrigin("*")

public class GetAllCartController {
    IConsultCartInput IConsultCartInput;

    @Autowired
    public GetAllCartController(IConsultCartInput IConsultCartInput){
        this.IConsultCartInput=IConsultCartInput;
    }

    @GetMapping()

    public ResponseEntity<List<ProductCartDTO>> GetAllUsers(){
        Collection<Cart> list=IConsultCartInput.getAllCart();

        List<ProductCartDTO> list1=list.stream().map(ProductCartDTO::coreToDTO).toList();

        if (!list1.isEmpty()) {
            return ResponseEntity.ok(list1);
        } else {
            return ResponseEntity.noContent().build();
        }
    }


}
