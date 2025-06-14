package com.app.erp.user.controller;

import com.app.erp.entity.user.Role;
import com.app.erp.entity.user.User;
import com.app.erp.dto.customer.UserCustomerDTO;
import com.app.erp.user.UserNotFoundException;
import com.app.erp.user.service.AdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }


    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Integer id,
            @RequestBody UserCustomerDTO userDTO
    ) {
        try {
            User updatedUser = adminService.updateUser(id, userDTO);
            return ResponseEntity.ok(updatedUser);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/roles")
    public List<Role> getAllRoles() {
        return adminService.listRoles();
    }

    @GetMapping("/users")
    public ResponseEntity<Page<UserCustomerDTO>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Page<UserCustomerDTO> usersPage = adminService.getAllUsersWithCustomers(PageRequest.of(page, size));
        return ResponseEntity.ok(usersPage);
    }

    @GetMapping("user-count")
    public long getCountCustomer() {
        return adminService.getUserCount();
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Integer id) {
        try {
            adminService.delete(id);
            return ResponseEntity.ok("User deleted successfully");
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
}



}
