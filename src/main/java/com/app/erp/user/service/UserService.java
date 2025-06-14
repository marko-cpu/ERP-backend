package com.app.erp.user.service;

import com.app.erp.entity.user.User;
import com.app.erp.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean verify(String verificationCode) {

        User user  = userRepository.findByVerificationCode(verificationCode);

        if (user == null || user.isEnabled()) {
            return false;
        } else {

            userRepository.enable(user.getId());
            return true;
        }


    }


}
