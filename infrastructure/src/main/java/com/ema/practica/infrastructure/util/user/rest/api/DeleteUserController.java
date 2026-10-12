package com.ema.practica.infrastructure.util.user.rest.api;


import com.ema.practica.infrastructure.util.user.entity.UserEntity;
import com.ema.practica.infrastructure.util.user.rest.model.UserDTO;
import com.ema.practica.infrastructure.util.user.useCases.DeleteUserUseCaseImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/deleteUser")

public class DeleteUserController {
    private final DeleteUserUseCaseImp deleteUserUseCaseImp;

    @Autowired
    public DeleteUserController(DeleteUserUseCaseImp deleteUserUseCaseImp){
        this.deleteUserUseCaseImp=deleteUserUseCaseImp;
    }

    @DeleteMapping("/{idUser}")
    public ResponseEntity<?> deleteUser(@PathVariable UUID idUser){

        boolean resultado=deleteUserUseCaseImp.deleteUser(idUser);
        if(resultado) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
