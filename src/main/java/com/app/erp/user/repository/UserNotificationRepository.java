package com.app.erp.user.repository;

import com.app.erp.entity.Notification;
import com.app.erp.entity.User;
import com.app.erp.entity.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
    Optional<UserNotification> findByUserAndNotification(User user, Notification notification);
    List<UserNotification> findByUserAndNotificationIn(User user, List<Notification> notifications);


}
