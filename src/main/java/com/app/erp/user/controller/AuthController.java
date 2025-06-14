package com.app.erp.user.controller;

import com.app.erp.entity.user.User;
import com.app.erp.entity.user.UserInfo;
import com.app.erp.security.JwtUtils;
import com.app.erp.security.UserDetails;

import com.app.erp.setting.EmailSettingBag;
import com.app.erp.setting.SettingService;
import com.app.erp.setting.Utility;
import com.app.erp.user.payload.request.LoginRequest;
import com.app.erp.user.payload.request.SignupRequest;
import com.app.erp.user.payload.response.JwtResponse;
import com.app.erp.user.payload.response.MessageResponse;
import com.app.erp.user.repository.UserInfoRepository;
import com.app.erp.user.repository.UserRepository;


import com.app.erp.user.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.validation.Valid;
import net.bytebuddy.utility.RandomString;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;


import java.io.UnsupportedEncodingException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/auth")
@CrossOrigin("http://localhost:3000")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UserService userService;
    private final UserInfoRepository userInfoRepository;
    private final PasswordEncoder passwordEncoder;
    private final SettingService settingService;
    private final JwtUtils jwtUtils;

    public AuthController(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            UserService userService,
            UserInfoRepository userInfoRepository,
            PasswordEncoder passwordEncoder,
            SettingService settingService,
            JwtUtils jwtUtils
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.userService = userService;
        this.userInfoRepository = userInfoRepository;
        this.passwordEncoder = passwordEncoder;
        this.settingService = settingService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        User user = userRepository.findByEmail(loginRequest.getEmail());

        if (user == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Error: User not found."));
        }
        if (!user.isEnabled()) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Error: User is not activated."));
        }
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getUsername(),
                userDetails.getFullname(),
                roles));
    }

    @PostMapping("/signup")
    @Transactional
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) throws MessagingException, UnsupportedEncodingException {

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }
        User user = new User();
        user.setFirstName(signUpRequest.getFirstName());
        user.setLastName(signUpRequest.getLastName());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setEnabled(false);

        String randomCode = RandomString.make(64);
        user.setVerificationCode(randomCode);

//        Role existingRole = roleRepository.findByName("CUSTOMER");
//         if (existingRole != null) {
//            user.getRoles().add(existingRole);
//        } else {
//            throw new RuntimeException("Role not found");
//        }

        userRepository.save(user);

        UserInfo customer = new UserInfo();
        customer.setUser(user);
        customer.setPhoneNumber(signUpRequest.getPhoneNumber());
        customer.setAddress(signUpRequest.getAddress());
        customer.setCity(signUpRequest.getCity());
        customer.setPostalCode(signUpRequest.getPostalCode());

        userInfoRepository.save(customer);
        sendVerificationEmail(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }


    private void sendVerificationEmail(User user)
            throws UnsupportedEncodingException, MessagingException {
        EmailSettingBag emailSettings = settingService.getEmailSettings();
        JavaMailSenderImpl mailSender = Utility.prepareMailSender(emailSettings);

        String toAddress = user.getEmail();
        String subject = emailSettings.getCustomerVerifySubject();
        String content = emailSettings.getCustomerVerifyContent();

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(emailSettings.getFromAddress(), emailSettings.getSenderName());
        helper.setTo(toAddress);
        helper.setSubject(subject);

        content = content.replace("[[name]]", user.getFullName());

        String verifyURL = "http://localhost:3000/verify?code="  + user.getVerificationCode();

        content = content.replace("[[URL]]", verifyURL);

        helper.setText(content, true);

        mailSender.send(message);

        System.out.println("to Address: " + toAddress);
        System.out.println("Verify URL: " + verifyURL);
    }

    @GetMapping("/verify")
    public ResponseEntity<Map<String, String>> verifyAccount(@RequestParam("code") String code) {
        boolean verified = userService.verify(code);
        Map<String, String> response = new HashMap<>();

        if (verified) {
            response.put("message", "Successful Verfication! You can log in.");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Verification failed, Invalid or used code.");
            return ResponseEntity.badRequest().body(response);
        }
    }










}
