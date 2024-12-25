package com.ema.practica.core.detailOrder.useCases;

import com.ema.practica.core.detailOrder.input.IUpdateDetailOrderInput;
import com.ema.practica.core.detailOrder.model.DetailOrder;
import com.ema.practica.core.detailOrder.output.IRepositoryUpdateDetailOrder;
import com.ema.practica.core.product.model.Product;

import java.util.List;

public class UpdateDetailOrderUseCase  implements IUpdateDetailOrderInput {
    IRepositoryUpdateDetailOrder iRepositoryUpdateDetailOrder;
    public  UpdateDetailOrderUseCase(IRepositoryUpdateDetailOrder iRepositoryUpdateDetailOrder){
        this.iRepositoryUpdateDetailOrder=iRepositoryUpdateDetailOrder;
    }
    @Override
    public boolean updateDetailOrder(Product productList, int amount, double priceUnitary, double subtotalPrice, Long id) {
        DetailOrder detailOrder1=iRepositoryUpdateDetailOrder.currentDetailOrder(id);
        detailOrder1.setProductList(productList);
        detailOrder1.setAmount(amount);
        detailOrder1.setPriceUnitary(priceUnitary);
        detailOrder1.setSubtotalPrice(subtotalPrice);
        return iRepositoryUpdateDetailOrder.updateDetailOrder(detailOrder1,id);
    }
}
