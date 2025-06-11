package com.app.erp.user.controller;

import com.app.erp.entity.user.Role;
import com.app.erp.entity.user.User;
import com.app.erp.dto.customer.UserCustomerDTO;
import com.app.erp.user.service.AdminService;
import com.app.erp.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
public class UserRestController {

    private final AdminService service;
    private final UserService userService;

    public UserRestController(AdminService service, UserService userService) {
        this.service = service;
        this.userService = userService;
    }

    @GetMapping("/users/id")
    public ResponseEntity<Integer> getUserIdByEmail(@Param("email") String email) {
        User user = userService.getUserByEmail(email);

    if (user == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    return ResponseEntity.ok(user.getId());
}



    @PostMapping("/users/check_email")
    public String checkDuplicateEmail(@Param("id") Integer id, @Param("email") String email) {

        return service.isEmailUnique(id, email) ? "OK" : "Duplicated";
    }

    @GetMapping("/users/me")
    public ResponseEntity<UserCustomerDTO> getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        UserCustomerDTO dto = convertUserToDTO(user);
        return ResponseEntity.ok(dto);
    }

    private UserCustomerDTO convertUserToDTO(User user) {
        UserCustomerDTO dto = new UserCustomerDTO();
        dto.setUserId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEnabled(user.isEnabled());

        if (user.getCustomer() != null) {
            dto.setPhoneNumber(user.getCustomer().getPhoneNumber());
            dto.setAddress(user.getCustomer().getAddress());
            dto.setCity(user.getCustomer().getCity());
            dto.setPostalCode(user.getCustomer().getPostalCode());
            dto.setCreatedTime(user.getCustomer().getCreatedTime());
        }

        dto.setRoles(user.getRoles().stream()
                .map(role -> new Role(role.getId(), role.getName(), role.getDescription()))
                .collect(Collectors.toList()));

        return dto;
    }




}
