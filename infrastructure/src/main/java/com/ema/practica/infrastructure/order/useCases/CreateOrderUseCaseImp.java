package com.ema.practica.infrastructure.order.useCases;

import com.ema.practica.core.order.model.Order;
import com.ema.practica.core.order.output.IRepositoryCreateOrder;
import com.ema.practica.infrastructure.cart.crud.IConsultCartCrud;
import com.ema.practica.infrastructure.cart.entity.CartEntity;
import com.ema.practica.infrastructure.cart.useCases.AddProductCartUseCaseImp;
import com.ema.practica.infrastructure.detailOrder.entity.DetailOrderEntity;
import com.ema.practica.infrastructure.detailOrder.useCases.CreateDetailUseCaseImp;

import com.ema.practica.infrastructure.order.crud.ICreateOrderCrud;
import com.ema.practica.infrastructure.order.entity.OrderEntity;
import com.ema.practica.infrastructure.order.rest.model.OrderRequestBody;
import com.ema.practica.infrastructure.user.crud.IListUserCrud;
import com.ema.practica.infrastructure.user.entitty.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Transactional
@Service
public class CreateOrderUseCaseImp  implements IRepositoryCreateOrder {
    private final ICreateOrderCrud iCreateOrderCrud;
    private final AddProductCartUseCaseImp addProductCartUseCaseImp;
    private final IConsultCartCrud iConsultCartCrud;
    private final IListUserCrud iListUserCrud;
    private  final CreateDetailUseCaseImp createDetailUseCaseImp;

    @Autowired
    public CreateOrderUseCaseImp(ICreateOrderCrud iCreateOrderCrud, AddProductCartUseCaseImp addProductCartUseCaseImp, IConsultCartCrud iConsultCartCrud, IListUserCrud iListUserCrud, CreateDetailUseCaseImp createDetailUseCaseImp){
        this.iCreateOrderCrud = iCreateOrderCrud;
        this.addProductCartUseCaseImp = addProductCartUseCaseImp;
        this.iConsultCartCrud = iConsultCartCrud;
        this.iListUserCrud = iListUserCrud;
        this.createDetailUseCaseImp=createDetailUseCaseImp;
    }
    @Override
    public boolean saveOrder(Order order) {
        OrderEntity orderEntity= OrderEntity.coreToEntity(order);
        return iCreateOrderCrud.save(orderEntity).getIdOrder()!=null;
    }

    public void createOrder(OrderRequestBody orderRequestBody){
        List<CartEntity> carrito =orderRequestBody.getListCart();
        UserEntity userEntity=carrito.getFirst().getUser();


        System.out.println(userEntity.toString());
        if (carrito.isEmpty()) {
            throw new IllegalArgumentException("El carrito está vacío.");
        }
        // Crear la orden
        OrderEntity orden = new OrderEntity();
        orden.setUser(userEntity);
        orden.setOrderDate(LocalDate.now());
        orden.setStatus("Pendiente");

        List<DetailOrderEntity> detalles = new ArrayList<>();
        double total = 0.0;

        for (CartEntity item : carrito) {
            DetailOrderEntity detalle = new DetailOrderEntity();
            detalle.setProduct(item.getProduct());
            detalle.setAmount(item.getCantidad());
            detalle.setPriceUnitary(item.getProduct().getPriceUnitary());
            double subtotal = item.getCantidad() * item.getProduct().getPriceUnitary();
            detalle.setSubtotalPrice(subtotal);
            detalles.add(detalle);
            total += subtotal;
        }
        orden.setDetailOrders(detalles);
        orden.setTotalAmount(total);
        iCreateOrderCrud.save(orden);
        this.iConsultCartCrud.deleteAll(carrito);

    }
}