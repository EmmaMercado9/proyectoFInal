package com.ema.practica.infrastructure.detailOrder.useCases;

import com.ema.practica.core.detailOrder.model.DetailOrder;
import com.ema.practica.core.detailOrder.output.IRepositoryUpdateDetailOrder;
import com.ema.practica.infrastructure.detailOrder.crud.IUpdateDetailOrderCrud;
import com.ema.practica.infrastructure.detailOrder.entity.DetailOrderEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UpdateDetailOrderUseCaseImp implements IRepositoryUpdateDetailOrder {

    IUpdateDetailOrderCrud iUpdateDetailOrderCrud;
    @Autowired
    public UpdateDetailOrderUseCaseImp (IUpdateDetailOrderCrud iUpdateDetailOrderCrud){
        this.iUpdateDetailOrderCrud=iUpdateDetailOrderCrud;
    }
    @Override
    public DetailOrder currentDetailOrder(Long id) {
        DetailOrderEntity detailOrderEntity=iUpdateDetailOrderCrud.findById(id).orElse(null);
        assert detailOrderEntity != null;
        return (detailOrderEntity.entityToCore());
    }

    @Override
    public boolean updateDetailOrder(DetailOrder detailOrder, Long id) {
        DetailOrderEntity detailOrderEntity=iUpdateDetailOrderCrud.findById(id).orElse(null);
        if(detailOrderEntity!=null) {
            DetailOrderEntity detailOrder1 = DetailOrderEntity.coreToEntity(detailOrder);
            iUpdateDetailOrderCrud.save(detailOrder1);
            return true;
        }
        return false;
    }
}
