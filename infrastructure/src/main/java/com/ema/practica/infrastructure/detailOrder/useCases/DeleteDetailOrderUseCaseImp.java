package com.ema.practica.infrastructure.detailOrder.useCases;

import com.ema.practica.core.detailOrder.input.IDeleteDetailOrderInput;
import com.ema.practica.core.detailOrder.model.DetailOrder;
import com.ema.practica.core.detailOrder.output.IRepositoryDeleteDetailOrder;
import com.ema.practica.core.home.input.IDeleteHomeInput;
import com.ema.practica.infrastructure.detailOrder.crud.IDeleteDetailOrderCrud;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeleteDetailOrderUseCaseImp implements IRepositoryDeleteDetailOrder {

    private final IDeleteDetailOrderCrud deleteDetailOrderCrud;
    @Autowired
    public DeleteDetailOrderUseCaseImp(IDeleteDetailOrderCrud deleteDetailOrderCrud){
        this.deleteDetailOrderCrud=deleteDetailOrderCrud;
    }
    @Override
    public boolean deleteDetailOrder(Long id) {
        deleteDetailOrderCrud.deleteById(id);
        return true;
    }
}
