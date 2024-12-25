package com.ema.practica.infrastructure.cart.rest.api;

import com.ema.practica.core.cart.input.IAddProductCartInput;
import com.ema.practica.infrastructure.cart.entity.CartEntity;
import com.ema.practica.infrastructure.cart.rest.model.ProductCartDTO;
import com.ema.practica.infrastructure.cart.useCases.AddProductCartUseCaseImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin("*")

public class CreateCartController {

    IAddProductCartInput iAddProductCartInput;
    private final AddProductCartUseCaseImp addProductCartUseCaseImp;
    @Autowired
    public CreateCartController(IAddProductCartInput iAddProductCartInput, AddProductCartUseCaseImp addProductCartUseCaseImp){
        this.iAddProductCartInput=iAddProductCartInput;
        this.addProductCartUseCaseImp = addProductCartUseCaseImp;
    }

    @GetMapping("/getListByClient")
    public ResponseEntity<List<ProductCartDTO>> getListByClient(){
        UserDetails userDetails=(UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String name=userDetails.getUsername();
        return new ResponseEntity<>(this.addProductCartUseCaseImp.getListByUserName(name).stream().map(ProductCartDTO::coreToDTO).toList(),HttpStatus.OK);
    }
    @GetMapping("/count/{user_id}")
    public ResponseEntity<Long> countByUser(@PathVariable("user_id") Long user_id) {
    return new ResponseEntity<>(this.addProductCartUseCaseImp.getCountByUser(user_id),HttpStatus.OK);

    }

    @PostMapping("/addProductCar")
    public ResponseEntity<?> addProductCart( @RequestParam(name = "product_id") Long product_id,@RequestParam(name = "quantity") int quantity ) {
        if (product_id==null || quantity<=0) {
            Map<String, String> response = new HashMap<>();
            response.put("Message", "Revise los campos");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        UserDetails userDetails=(UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userName = userDetails.getUsername();
        this.addProductCartUseCaseImp.save(userName,product_id,quantity);
        Map<String, String> response = new HashMap<>();
        response.put("Message", "Producto Agregado");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @DeleteMapping("cleam/{item_id}")
    public ResponseEntity<?> removeProduct(@PathVariable("item_id")Long id){
        this.addProductCartUseCaseImp.removeProduct(id);
        Map<String, String> response = new HashMap<>();
        response.put("Message", "Producto Eliminado");

        return new ResponseEntity<>(response,HttpStatus.OK);
    }
}
