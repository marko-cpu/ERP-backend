package com.app.erp.user.repository;


import com.app.erp.entity.user.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserInfoRepository extends  JpaRepository<UserInfo, Integer> , CrudRepository<UserInfo, Integer> {

}
