package com.app.erp.user.repository;

import com.app.erp.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<User, Integer>{

    @Query("SELECT u FROM User u WHERE u.email = :email")
    public User getUserByEmail(@Param("email") String email);

    User findByEmail(String email);

    @Query("SELECT u FROM User u JOIN FETCH u.customer")
    List<User> findAllUsersWithCustomers();

    @Query(value = "SELECT u FROM User u LEFT JOIN FETCH u.customer",
            countQuery = "SELECT COUNT(u) FROM User u")
    Page<User> findAllWithCustomer(Pageable pageable);

    Boolean existsByEmail(String email);

    public Long countById(Integer id);

    @Query("SELECT c FROM User c WHERE c.verificationCode = ?1")
    public User findByVerificationCode(String code);


    @Modifying()
    @Query("UPDATE User c SET c.verificationCode = null WHERE c.id = ?1")
    public void enable(Integer id);





}
