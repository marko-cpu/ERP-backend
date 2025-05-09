package com.app.erp.user.repository;

import com.app.erp.entity.notification.Notification;
import com.app.erp.entity.user.User;
import com.app.erp.entity.notification.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
    Optional<UserNotification> findByUserAndNotification(User user, Notification notification);
    List<UserNotification> findByUserAndNotificationIn(User user, List<Notification> notifications);


}
