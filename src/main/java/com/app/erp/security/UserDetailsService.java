package com.app.erp.security;


import com.app.erp.entity.User;
import com.app.erp.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {


    @Autowired
    private UserRepository userRepo;



    @Override
    public org.springframework.security.core.userdetails.UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepo.getUserByEmail(email);


        if (user != null) {
            return new UserDetails(user);
        }


        throw new UsernameNotFoundException("Could not find user with email: " + email);
    }
}
