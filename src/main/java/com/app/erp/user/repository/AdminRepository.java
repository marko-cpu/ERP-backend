package com.app.erp.user.repository;

import com.app.erp.entity.user.User;

import org.springframework.data.repository.CrudRepository;

public interface AdminRepository extends CrudRepository<User, Integer> {

}
