package com.ema.practica.core.detailOrder.useCases;

import com.ema.practica.core.detailOrder.input.ICreateDetailOrderInput;
import com.ema.practica.core.detailOrder.model.DetailOrder;
import com.ema.practica.core.detailOrder.output.IRepositoryCreateDetailOrde;

public class CreateDetailUseCase implements ICreateDetailOrderInput {

    IRepositoryCreateDetailOrde iRepositoryCreateDetailOrde;
    public CreateDetailUseCase(IRepositoryCreateDetailOrde iRepositoryCreateDetailOrde) {
        this.iRepositoryCreateDetailOrde = iRepositoryCreateDetailOrde;
    }
    @Override
    public boolean createDetailOrder(DetailOrder detail) {
        return iRepositoryCreateDetailOrde.saveDetail(detail) ;
    }
}
