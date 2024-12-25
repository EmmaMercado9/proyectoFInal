package com.ema.practica.core.cart.useCases;

import com.ema.practica.core.cart.input.IAddProductCartInput;
import com.ema.practica.core.cart.model.Cart;
import com.ema.practica.core.cart.output.IRepositoryAddProductCart;

public class AddProductCartUseCase implements IAddProductCartInput {
    IRepositoryAddProductCart iRepositoryAddProductCart;
    public  AddProductCartUseCase (IRepositoryAddProductCart iRepositoryAddProductCart){
        this.iRepositoryAddProductCart = iRepositoryAddProductCart;  // Inyectar dependencia IRepositoryAddProductCart en el constructor de AddProductCartUseCase  // Agregar logica de negocio para agregar producto al carrito  // Llamar al método de la interfaz IRepositoryAddProductCart para realizar la acción en la base de datos  // Retornar el resultado de la acción (true si se agregó correctamente, false en caso contrario)  // Implementar el método addProductCart en la clase que implementa IRepositoryAddProductCart  // Ejecutar la lógica de negocio y llamar al método addProductCart en la clase AddProductCartUseCase  // Retornar el resultado de la acción al método addProductCartInput.addProductCart()  // Ejemplos de uso: Cart cart = Cart.createCart(1L, user,)
    }
    @Override
    public Cart addProductCart(String name,Long idProduct,int cantidad) throws Exception {
        return  iRepositoryAddProductCart.save(name,idProduct,cantidad);

    }
}
