package com.ema.practica.infrastructure.user.rest.api;

import com.ema.practica.core.user.input.IModifyUserInput;

import com.ema.practica.core.user.model.User;
import com.ema.practica.infrastructure.user.rest.model.UserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/modify")
@CrossOrigin("*")

public class ModifyUserController {
    IModifyUserInput iModifyUserInput;


    public  ModifyUserController(IModifyUserInput iModifyUserInput){
        this.iModifyUserInput=iModifyUserInput;
    }

    @PutMapping
    public ResponseEntity<?> modifyUser(@RequestBody UserDTO userDTO) throws Exception {
        User user=userDTO.dtoToCore();
        boolean result = iModifyUserInput.ModifyUser(user.getName(),user.getPassword(),user.getMail(),user.getHome(),user.getIdUser());
        return ResponseEntity.ok().body(result);
    }

}
