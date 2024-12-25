package com.ema.practica.infrastructure.detailOrder.rest.api;
import com.ema.practica.core.detailOrder.input.IUpdateDetailOrderInput;
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
@RequestMapping("/updateDetailOrders")
@CrossOrigin("*")

public class UpdateDetailController {

    private final IConsultOrderCrud iConsultOrderCrud;
    private final IListProductCrud iListProductCrud;
    IUpdateDetailOrderInput iUpdateDetailOrderInput;
    @Autowired
    public UpdateDetailController(IConsultOrderCrud iConsultOrderCrud, IListProductCrud iListProductCrud, IUpdateDetailOrderInput iUpdateDetailOrderInput){
        this.iConsultOrderCrud = iConsultOrderCrud;
        this.iListProductCrud = iListProductCrud;
        this.iUpdateDetailOrderInput=iUpdateDetailOrderInput;
    }

    @PutMapping
    public ResponseEntity<?> modifyUser(@RequestBody DetailOrderDTO detailOrderDTO) {
        Product product =  iListProductCrud.findById(detailOrderDTO.getProductId()).get().entityToCore();
        DetailOrder detailOrder=detailOrderDTO.dtoToCore(product);
        boolean result = iUpdateDetailOrderInput.updateDetailOrder(
            detailOrder.getProductList(),
                detailOrder.getAmount(),
                detailOrder.getPriceUnitary(),
                detailOrder.getSubtotalPrice(),
                detailOrder.getIdDetailOrder()
                );
        return ResponseEntity.ok().body(result);
    }
}
