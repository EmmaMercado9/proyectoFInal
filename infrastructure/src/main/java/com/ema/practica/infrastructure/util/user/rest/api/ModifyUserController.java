package com.ema.practica.infrastructure.util.user.rest.api;

import com.ema.practica.infrastructure.util.user.entitty.UserEntity;
import com.ema.practica.infrastructure.util.user.rest.model.UserDTO;
import com.ema.practica.infrastructure.util.user.useCases.ModifyUserUseCaseImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/modify")
@CrossOrigin("*")

public class ModifyUserController {

    private final ModifyUserUseCaseImp modifyUserUseCaseImp;
    @Autowired
    public  ModifyUserController(ModifyUserUseCaseImp modifyUserUseCaseImp){
        this.modifyUserUseCaseImp=modifyUserUseCaseImp;
    }

    @PutMapping
    public ResponseEntity<?> modifyUser(@RequestBody UserDTO userDTO) throws Exception {
        UserEntity user=userDTO.dtoToEntity();
        boolean result = modifyUserUseCaseImp.modifyUser(user,user.getIdUser());
        return ResponseEntity.ok().body(result);
    }

}
