package com.ema.practica.infrastructure.servicesDetailUser;

import com.ema.practica.infrastructure.user.crud.ILoginUserCrud;
import com.ema.practica.infrastructure.user.entitty.SecurityUser;
import com.ema.practica.infrastructure.user.entitty.UserEntity;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class UserDetailServiceImp  implements UserDetailsService {
    @Autowired
    ILoginUserCrud iLoginUserCrud;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity=iLoginUserCrud.findFirstByMail(username);
        if(userEntity==null){
            throw new UsernameNotFoundException("User not found");
        }
        return new SecurityUser(userEntity);
    }
}
