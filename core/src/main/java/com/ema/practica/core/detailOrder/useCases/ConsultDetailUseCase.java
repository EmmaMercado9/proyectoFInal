package com.ema.practica.core.detailOrder.useCases;

import com.ema.practica.core.detailOrder.input.IConsultDetailOrderInput;
import com.ema.practica.core.detailOrder.model.DetailOrder;
import com.ema.practica.core.detailOrder.output.IRepositoryConsultDetail;

import java.util.Collection;

public class ConsultDetailUseCase implements IConsultDetailOrderInput {
    IRepositoryConsultDetail iRepositoryConsultDetail;

    public ConsultDetailUseCase(IRepositoryConsultDetail iRepositoryConsultDetail){
        this.iRepositoryConsultDetail = iRepositoryConsultDetail;
    }
    @Override
    public Collection<DetailOrder> getDetailOrder() {
        return iRepositoryConsultDetail.getdetailOrder();
    }
}
