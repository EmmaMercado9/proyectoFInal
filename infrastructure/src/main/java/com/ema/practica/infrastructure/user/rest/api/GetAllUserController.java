package com.ema.practica.infrastructure.user.rest.api;

import com.ema.practica.core.user.input.IListUserInput;
import com.ema.practica.core.user.model.User;
import com.ema.practica.infrastructure.home.crud.IConsultHomeCrud;
import com.ema.practica.infrastructure.home.entity.HomeEntity;
import com.ema.practica.infrastructure.home.rest.model.HomeDTO;
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
    IListUserInput iListUserInput;

    private IListUserCrud iListUserCrud;
    private final ListUserUseCaseImp listUserUseCaseImp;
    private IConsultHomeCrud iConsultHomeCrud;
    @Autowired
    public GetAllUserController(IListUserInput iListUserInput, ListUserUseCaseImp listUserUseCaseImp){
        this.iListUserInput=iListUserInput;
        this.listUserUseCaseImp = listUserUseCaseImp;
    }

    @GetMapping("/all")

    public ResponseEntity<List<UserDTO>> GetAllUsers(){
        Collection<User> list=iListUserInput.getAllUsers();

        List<UserDTO> list1=list.stream().map(UserDTO::coreToDTO).toList();

        if (!list1.isEmpty()) {
            return ResponseEntity.ok(list1);
        } else {
            return ResponseEntity.noContent().build();
        }
    }
    @PutMapping("/{userId}/home/{idhome}")
    public ResponseEntity<?> updateUserHome(@PathVariable Long userId, @PathVariable Long idhome) {
        UserEntity userEntity = iListUserCrud.findByIdUser(userId);
        if (userEntity != null) {

            HomeEntity home = iConsultHomeCrud.findByIdHome(userEntity.getHome().getIdHome());
            userEntity.setHome(home);
            iListUserCrud.save(userEntity);
            return ResponseEntity.ok(UserDTO.coreToDTO((userEntity).entityToCore()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/{userId}/home")
    public ResponseEntity<?> getUserHome(@PathVariable Long userId) {
        UserEntity userEntity = iListUserCrud.findByIdUser(userId);
        if (userEntity != null && userEntity.getHome().getIdHome() != null) {
            HomeEntity home = iConsultHomeCrud.findByIdHome(userEntity.getHome().getIdHome());
            return home != null ? ResponseEntity.ok(HomeDTO.coreToDTO(Objects.requireNonNull((home).entityToCore()))) : ResponseEntity.notFound().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/current-user")
    public ResponseEntity<?> getCurrentUser() {
        UserDetails userDetails=(UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username=userDetails.getUsername();
        UserDTO userDTO=UserDTO.coreToDTO(listUserUseCaseImp.getUserById(username));
        return new ResponseEntity<>(userDTO,HttpStatus.OK);
    }

}
