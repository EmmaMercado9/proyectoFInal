package com.ema.practica.infrastructure.util.servicesDetailUser;

import com.ema.practica.infrastructure.util.user.crud.ILoginUserCrud;
import com.ema.practica.infrastructure.util.user.entity.SecurityUser;
import com.ema.practica.infrastructure.util.user.entity.UserEntity;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class UserDetailServiceImp  implements UserDetailsService {
    private final ILoginUserCrud iLoginUserCrud;
    @Autowired
    public  UserDetailServiceImp(ILoginUserCrud iLoginUserCrud){
        this.iLoginUserCrud=iLoginUserCrud;
    }

    @Override
    public UserDetails loadUserByUsername(String mail) throws UsernameNotFoundException {
        UserEntity userEntity=iLoginUserCrud.findUserEntityByMail(mail);
        if(userEntity==null){
            throw new UsernameNotFoundException("User not found");
        }
        return new SecurityUser(userEntity);
    }
}
