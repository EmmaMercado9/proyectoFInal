package com.ema.practica.infrastructure.detailOrder.rest.api;

import com.ema.practica.core.cart.input.IDeleteProductCartInput;
import com.ema.practica.core.cart.model.Cart;
import com.ema.practica.core.detailOrder.input.IDeleteDetailOrderInput;
import com.ema.practica.core.detailOrder.model.DetailOrder;
import com.ema.practica.core.order.model.Order;
import com.ema.practica.core.product.model.Product;
import com.ema.practica.infrastructure.detailOrder.rest.model.DetailOrderDTO;
import com.ema.practica.infrastructure.order.crud.IConsultOrderCrud;
import com.ema.practica.infrastructure.product.crud.IListProductCrud;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/deleteDetail")
@CrossOrigin("*")

public class DeleteDetailController {
    private final IListProductCrud iListProductCrud;
    private final IConsultOrderCrud iConsultOrderCrud;
    IDeleteDetailOrderInput IDeleteDetailOrderInput;
    @Autowired
    public DeleteDetailController(IListProductCrud iListProductCrud, IConsultOrderCrud iConsultOrderCrud, IDeleteDetailOrderInput IDeleteDetailOrderInput){
        this.iListProductCrud = iListProductCrud;
        this.iConsultOrderCrud = iConsultOrderCrud;
        this.IDeleteDetailOrderInput=IDeleteDetailOrderInput;
    }

    @DeleteMapping()
    public ResponseEntity<?> deleteDetailOrder(@RequestBody DetailOrderDTO detailOrderDTO){
        Product product = ( iListProductCrud.findById(detailOrderDTO.getProductId()).get()).entityToCore();
        DetailOrder detailOrder=detailOrderDTO.dtoToCore(product);
        boolean resultado=IDeleteDetailOrderInput.deleteDetailOrder(detailOrder);
        return ResponseEntity.ok().body(resultado);
    }
}
