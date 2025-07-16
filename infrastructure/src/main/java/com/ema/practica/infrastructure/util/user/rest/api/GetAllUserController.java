package com.ema.practica.infrastructure.user.rest.api;

import com.ema.practica.infrastructure.user.crud.IListUserCrud;
import com.ema.practica.infrastructure.user.entitty.UserEntity;
import com.ema.practica.infrastructure.user.rest.model.UserDTO;

import com.ema.practica.infrastructure.user.useCases.ListUserUseCaseImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/getUser")
@CrossOrigin("*")

public class GetAllUserController {

    private final ListUserUseCaseImp listUserUseCaseImp;
    @Autowired
    public GetAllUserController(ListUserUseCaseImp listUserUseCaseImp){
        this.listUserUseCaseImp = listUserUseCaseImp;
    }

    @GetMapping("/all")

    public ResponseEntity<List<UserDTO>> GetAllUsers(){
        Collection<UserEntity> list=listUserUseCaseImp.getAll();

        List<UserDTO> list1=list.stream().map(UserDTO::entityToDTO).toList();

        if (!list1.isEmpty()) {
            return ResponseEntity.ok(list1);
        } else {
            return ResponseEntity.noContent().build();
        }
    }
    
    @GetMapping("/current-user")
    public ResponseEntity<?> getCurrentUser() {
        UserDetails userDetails=(UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username=userDetails.getUsername();
        UserDTO userDTO=UserDTO.entityToDTO(listUserUseCaseImp.getUserById(username));
        return new ResponseEntity<>(userDTO,HttpStatus.OK);
    }

}
