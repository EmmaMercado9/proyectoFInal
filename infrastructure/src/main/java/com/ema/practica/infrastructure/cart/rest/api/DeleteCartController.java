package com.ema.practica.infrastructure.cart.rest.api;

import com.ema.practica.core.cart.input.IDeleteProductCartInput;
import com.ema.practica.core.cart.model.Cart;
import com.ema.practica.core.product.input.IDeleteProductInput;
import com.ema.practica.core.product.model.Product;
import com.ema.practica.infrastructure.cart.rest.model.ProductCartDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/deleteCart")
@CrossOrigin("*")

public class DeleteCartController {

    IDeleteProductCartInput iDeleteProductCartInput;
    @Autowired
    public DeleteCartController(IDeleteProductCartInput iDeleteProductCartInput){
        this.iDeleteProductCartInput=iDeleteProductCartInput;
    }

    @DeleteMapping()
    public ResponseEntity<?> delete(@RequestBody ProductCartDTO productCartDTO){
        Cart cart=productCartDTO.dtoToCore();
        boolean resultado=iDeleteProductCartInput.deleteCartProduct(cart);
        return ResponseEntity.ok().body(resultado);
    }
}
