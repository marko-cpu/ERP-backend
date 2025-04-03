package com.app.erp.user.service;

import com.app.erp.entity.User;
import com.app.erp.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;




    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean verify(String verificationCode) {

        User user  = userRepository.findByVerificationCode(verificationCode);

        if (user == null || user.isEnabled()) {
            return false;
        } else {

            userRepository.enable(user.getId());
            //userRepository.save(user);
            return true;
        }


    }


}
