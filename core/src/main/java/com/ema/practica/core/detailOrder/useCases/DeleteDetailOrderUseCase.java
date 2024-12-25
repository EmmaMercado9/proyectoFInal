package com.ema.practica.core.detailOrder.useCases;

import com.ema.practica.core.detailOrder.input.IDeleteDetailOrderInput;
import com.ema.practica.core.detailOrder.model.DetailOrder;
import com.ema.practica.core.detailOrder.output.IRepositoryDeleteDetailOrder;

public class DeleteDetailOrderUseCase implements IDeleteDetailOrderInput {
    IRepositoryDeleteDetailOrder iRepositoryDeleteDetailOrder;
    public DeleteDetailOrderUseCase(IRepositoryDeleteDetailOrder iRepositoryDeleteDetailOrder) {
        this.iRepositoryDeleteDetailOrder = iRepositoryDeleteDetailOrder;
    }
    @Override
    public boolean deleteDetailOrder(DetailOrder Detail) {
        return iRepositoryDeleteDetailOrder.deleteDetailOrder(Detail.getIdDetailOrder()) ;
    }
}
