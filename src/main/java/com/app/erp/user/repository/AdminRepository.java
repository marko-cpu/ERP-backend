package com.app.erp.user.repository;

import com.app.erp.entity.User;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface AdminRepository extends CrudRepository<User, Integer> {

//    @Modifying()
//    @Query("UPDATE User c SET c.enabled = true WHERE c.id = ?1")
//    public void enable(Integer id);
}
