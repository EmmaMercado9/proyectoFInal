package com.ema.practica.infrastructure.user.rest.api;


import com.ema.practica.infrastructure.user.entitty.UserEntity;
import com.ema.practica.infrastructure.user.rest.model.UserDTO;
import com.ema.practica.infrastructure.user.useCases.DeleteUserUseCaseImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/deleteUser")
@CrossOrigin("*")

public class DeleteUserController {
    private final DeleteUserUseCaseImp deleteUserUseCaseImp;

    @Autowired
    public DeleteUserController(DeleteUserUseCaseImp deleteUserUseCaseImp){
        this.deleteUserUseCaseImp=deleteUserUseCaseImp;
    }

    @DeleteMapping()
    public ResponseEntity<?> deleteUser(@RequestBody UserDTO userDTO){
        UserEntity user=userDTO.dtoToEntity();
        boolean resultado=deleteUserUseCaseImp.deleteUser(user.getIdUser());
        return ResponseEntity.ok().body(resultado);
    }
}
