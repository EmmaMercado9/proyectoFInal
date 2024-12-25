package com.ema.practica.core.detailOrder.output;

import com.ema.practica.core.detailOrder.model.DetailOrder;

import java.util.Collection;

public interface IRepositoryConsultDetail {

    Collection<DetailOrder> getdetailOrder();
}
