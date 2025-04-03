package com.app.erp.user.service;

import com.app.erp.entity.Role;
import com.app.erp.entity.User;
import com.app.erp.entity.UserCustomerDTO;
import com.app.erp.entity.UserInfo;
import com.app.erp.goods.repository.ProductRepository;
import com.app.erp.sales.repository.OrderRepository;
import com.app.erp.user.RoleRepository;
import com.app.erp.user.UserNotFoundException;
import com.app.erp.user.repository.AdminRepository;
import com.app.erp.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private RoleRepository roleRepo;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;


    @Autowired
    private PasswordEncoder passwordEncoder;

//    public User updateUserRoles(Integer id, List<Role> roles) throws UserNotFoundException {
//        User user = userRepo.findById(id)
//                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
//
//        // Obriši postojeće role i dodaj nove
//        user.getRoles().clear();
//        for (Role role : roles) {
//            Role existingRole = roleRepo.findById(role.getId())
//                    .orElseThrow(() -> new RuntimeException("Role not found: " + role.getName()));
//            user.getRoles().add(existingRole);
//        }
//
//        return userRepo.save(user);
//    }

public UserCustomerDTO getUserById(Integer id) throws UserNotFoundException {
    User user = userRepo.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

    // Convert User to UserCustomerDTO
    UserCustomerDTO dto = new UserCustomerDTO();
    dto.setUserId(user.getId());
    dto.setEmail(user.getEmail());
    dto.setFirstName(user.getFirstName());
    dto.setLastName(user.getLastName());
    dto.setEnabled(user.isEnabled());

    // Populate customer details
    if (user.getCustomer() != null) {
        dto.setCustomerId(user.getCustomer().getId());
        dto.setAddress(user.getCustomer().getAddress());
        dto.setCity(user.getCustomer().getCity());
        dto.setPostalCode(user.getCustomer().getPostalCode());
        dto.setPhoneNumber(user.getCustomer().getPhoneNumber());
    }

    return dto;
}


    public User updateUser(Integer id, UserCustomerDTO userDTO) throws UserNotFoundException {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        // Ažuriraj osnovne podatke
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setEnabled(userDTO.isEnabled());

        // Ažuriraj role
        user.getRoles().clear();
        for (Role role : userDTO.getRoles()) {
            Role existingRole = roleRepo.findById(role.getId())
                    .orElseThrow(() -> new RuntimeException("Role not found: " + role.getName()));
            user.getRoles().add(existingRole);
        }

        // Ažuriraj customer podatke
        UserInfo customer = user.getCustomer();
        if (customer != null) {
            customer.setPhoneNumber(userDTO.getPhoneNumber());
            customer.setAddress(userDTO.getAddress());
            customer.setCity(userDTO.getCity());
            customer.setPostalCode(userDTO.getPostalCode());
        }

        return userRepo.save(user);
    }

    public Iterable<User> getAllUsers()  {
        return adminRepository.findAll();
    }

    public long getUserCount()  {
        return userRepository.count();
    }
    public User save(User user) {
        boolean isUpdatingUser = (user.getId() != null);

        if(isUpdatingUser) {
            User existingUser = userRepo.findById(user.getId()).get();

            if (user.getPassword().isEmpty()) {
                user.setPassword(existingUser.getPassword());
            } else {
                encodePassword(user);
            }

        } else {
            encodePassword(user);
        }

        return userRepo.save(user);

    }

    private void encodePassword(User user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
    }

    public List<Role> listRoles() {

        return (List<Role>) roleRepo.findAll();

    }

    public Page<UserCustomerDTO> getAllUsersWithCustomers(Pageable pageable) {
        Page<User> usersPage = userRepo.findAllWithCustomer(pageable);
        return usersPage.map(this::convertToUserCustomerDTO); // Конвертуј сваки ентитет
    }

    private UserCustomerDTO convertToUserCustomerDTO(User user) {
        UserCustomerDTO dto = new UserCustomerDTO();

        // Мапирање основних података
        dto.setUserId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEnabled(user.isEnabled());

        // Мапирање података о купцу (ако постоји)
        if (user.getCustomer() != null) {
            dto.setCustomerId(user.getCustomer().getId());
            dto.setAddress(user.getCustomer().getAddress());
            dto.setCity(user.getCustomer().getCity());
            dto.setPostalCode(user.getCustomer().getPostalCode());
            dto.setPhoneNumber(user.getCustomer().getPhoneNumber());
            dto.setCreatedTime(user.getCustomer().getCreatedTime());
        }

        // Мапирање улога
        List<Role> roles = user.getRoles().stream()
                .map(role -> {
                    Role roleDTO = new Role();
                    roleDTO.setId(role.getId());
                    roleDTO.setName(role.getName());
                    roleDTO.setDescription(role.getDescription());
                    return roleDTO;
                })
                .collect(Collectors.toList());
        dto.setRoles(roles);

        return dto;
    }


    public boolean isEmailUnique(Integer id, String email) {
        User userByEmail = userRepo.getUserByEmail(email);

        if (userByEmail == null) return true;

        boolean isCreatingNew = (id == null);

        if (isCreatingNew) {
            if (userByEmail != null) return false;
        } else {
            if(userByEmail.getId() != id) {
                return false;
            }
        }

        return true;

    }

    public User get(Integer id) throws UserNotFoundException {
        try {
            return userRepo.findById(id).get();
        } catch (NoSuchElementException ex) {
            throw new UserNotFoundException("Could not find any user with ID " + id);
        }

    }

    public void delete(Integer id) throws UserNotFoundException {
        Long countById = userRepo.countById(id);
        if (countById == null || countById == 0) {
            throw new UserNotFoundException("Could not find any user with ID " + id);
        }

        userRepo.deleteById(id);
    }




}
