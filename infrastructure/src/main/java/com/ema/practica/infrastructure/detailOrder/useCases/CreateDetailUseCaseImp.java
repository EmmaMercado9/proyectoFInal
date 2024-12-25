package com.ema.practica.infrastructure.detailOrder.useCases;

import com.ema.practica.core.detailOrder.model.DetailOrder;
import com.ema.practica.core.detailOrder.output.IRepositoryCreateDetailOrde;
import com.ema.practica.infrastructure.detailOrder.crud.ICreateDetailOrderCrud;
import com.ema.practica.infrastructure.detailOrder.entity.DetailOrderEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CreateDetailUseCaseImp implements IRepositoryCreateDetailOrde {

    ICreateDetailOrderCrud iCreateDetailOrderCrud;
    @Autowired
    public CreateDetailUseCaseImp(ICreateDetailOrderCrud iCreateDetailOrderCrud){
        this.iCreateDetailOrderCrud=iCreateDetailOrderCrud;
    }
    @Override

    public boolean saveDetail(DetailOrder detailOrder) {
        DetailOrderEntity detailOrderEntity= DetailOrderEntity.coreToEntity(detailOrder);
        iCreateDetailOrderCrud.save(detailOrderEntity);
        return true;
    }

    public void save(DetailOrderEntity detailOrderEntity){
        this.iCreateDetailOrderCrud.save(detailOrderEntity);
    }
}
