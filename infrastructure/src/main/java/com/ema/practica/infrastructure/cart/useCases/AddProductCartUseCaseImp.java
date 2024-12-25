package com.ema.practica.infrastructure.cart.useCases;

import com.ema.practica.core.cart.input.IAddProductCartInput;
import com.ema.practica.core.cart.model.Cart;
import com.ema.practica.core.cart.output.IRepositoryAddProductCart;
import com.ema.practica.core.order.model.Order;
import com.ema.practica.core.user.exceptions.ExeptionProductoNoEncontrado;
import com.ema.practica.infrastructure.cart.crud.IAddProductCartCrud;
import com.ema.practica.infrastructure.cart.crud.IDeleteProductCartCrud;
import com.ema.practica.infrastructure.cart.entity.CartEntity;
import com.ema.practica.infrastructure.detailOrder.entity.DetailOrderEntity;
import com.ema.practica.infrastructure.order.crud.ICreateOrderCrud;
import com.ema.practica.infrastructure.order.entity.OrderEntity;
import com.ema.practica.infrastructure.product.crud.IListProductCrud;
import com.ema.practica.infrastructure.product.entity.ProductEntity;
import com.ema.practica.infrastructure.servicesDetailUser.UserDetailServiceImp;
import com.ema.practica.infrastructure.user.crud.IListUserCrud;
import com.ema.practica.infrastructure.user.entitty.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AddProductCartUseCaseImp implements IRepositoryAddProductCart {
    private final IDeleteProductCartCrud iDeleteProductCartCrud;
    private final IAddProductCartCrud iAddProductCartCrud;
    private final IListProductCrud iListProductCrud;
    private  final IListUserCrud iListUserCrud;
    private final ICreateOrderCrud iCreateOrderCrud;
    private UserDetailServiceImp userDetailServiceImp;
    @Autowired
    public  AddProductCartUseCaseImp(IDeleteProductCartCrud iDeleteProductCartCrud, IAddProductCartCrud iAddProductCartCrud, IListProductCrud iListProductCrud, IListUserCrud iListUserCrud, ICreateOrderCrud iCreateOrderCrud) {
        this.iDeleteProductCartCrud = iDeleteProductCartCrud;
        this.iAddProductCartCrud = iAddProductCartCrud;
        this.iListProductCrud = iListProductCrud;
        this.iListUserCrud = iListUserCrud;
        this.iCreateOrderCrud = iCreateOrderCrud;
    }


    @Override
    public Cart save(String name ,Long idProduct,int cantidad) throws ExeptionProductoNoEncontrado{
        ProductEntity productEntity = null;
        Optional<ProductEntity> optionalProduct = iListProductCrud.findById(idProduct);
        if (optionalProduct.isPresent() ) {
            productEntity = optionalProduct.get();
        }
        if(productEntity == null) {
            throw new ExeptionProductoNoEncontrado("El producto no existe");        }

        UserEntity userEntity=null;
        if(name!=null){
            userEntity=iListUserCrud.findByMail(name);
        }

            if (userEntity != null) {
                CartEntity cartEntity = new CartEntity(userEntity, productEntity, cantidad);
                iAddProductCartCrud.save(cartEntity);
                return cartEntity.entityToCore();
            }

        return null;
    }

    public  List<Cart> getListByUser(Long idUser){
        return this.iAddProductCartCrud.findByUser_IdUser(idUser).stream().map(CartEntity::entityToCore).toList();

    }
    public  List<Cart> getListByUserName(String name){
        return this.iAddProductCartCrud.findByUser_Mail(name).stream().map(CartEntity::entityToCore).toList();

    }
    public void clearShoppingCart(Long idUser){
       this.iAddProductCartCrud.deleteByUser_IdUser(idUser);
    }
    public void removeProduct(Long id){
        this.iAddProductCartCrud.deleteById(id);
    }

    public Long getCountByUser(Long idUser){
        return this.iAddProductCartCrud.countByUser_IdUser(idUser);
    }

    public void saveCartEntity(CartEntity cartEntity){
        this.iAddProductCartCrud.save(cartEntity);
    }
}
