package com.ema.practica.infrastructure.util.user.rest.api;

import com.ema.practica.infrastructure.util.user.entity.UserEntity;
import com.ema.practica.infrastructure.util.user.rest.model.UserDTO;
import com.ema.practica.infrastructure.util.user.useCases.ModifyUserUseCaseImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/modify")

public class ModifyUserController {

    private final ModifyUserUseCaseImp modifyUserUseCaseImp;
    @Autowired
    public  ModifyUserController(ModifyUserUseCaseImp modifyUserUseCaseImp){
        this.modifyUserUseCaseImp=modifyUserUseCaseImp;
    }

    @PutMapping
    public ResponseEntity<?> modifyUser(@RequestBody UserDTO userDTO) throws Exception {

        boolean result = modifyUserUseCaseImp.modifyUser(userDTO,userDTO.getIdUser());
        if(result) {
            return ResponseEntity.ok().body(true);
        }
        return ResponseEntity.notFound().build();
    }

}
