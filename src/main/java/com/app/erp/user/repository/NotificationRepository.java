package com.app.erp.user.repository;

import com.app.erp.entity.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

   // List<Notification> findByRecipientRolesIn(List<String> roles);


    @Query("SELECT DISTINCT n FROM Notification n WHERE EXISTS (SELECT r FROM n.recipientRoles r WHERE r IN :roles)")
    List<Notification> findByRecipientRolesIn(@Param("roles") List<String> roles);

}