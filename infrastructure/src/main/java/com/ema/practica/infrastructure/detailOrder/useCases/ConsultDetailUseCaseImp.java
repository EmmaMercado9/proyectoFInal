package com.ema.practica.infrastructure.detailOrder.useCases;

import com.ema.practica.core.detailOrder.model.DetailOrder;
import com.ema.practica.core.detailOrder.output.IRepositoryConsultDetail;
import com.ema.practica.infrastructure.detailOrder.crud.IConsultDetailOrderCrud;
import com.ema.practica.infrastructure.detailOrder.entity.DetailOrderEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
@Service

public class ConsultDetailUseCaseImp implements IRepositoryConsultDetail {
    private final IConsultDetailOrderCrud iConsultDetailOrderCrud;
    @Autowired

    public ConsultDetailUseCaseImp(IConsultDetailOrderCrud iConsultDetailOrderCrud){
        this.iConsultDetailOrderCrud = iConsultDetailOrderCrud;
    }
    @Override
    public Collection<DetailOrder> getdetailOrder() {
        Iterable<DetailOrderEntity> detailOrderEntities = iConsultDetailOrderCrud.findAll();
        return StreamSupport.stream(detailOrderEntities.spliterator(), false).map(DetailOrderEntity::entityToCore).collect(Collectors.toList());

    }

}
