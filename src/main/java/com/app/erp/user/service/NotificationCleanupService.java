package com.app.erp.user.service;

import com.app.erp.entity.notification.Notification;
import com.app.erp.entity.notification.UserNotification;
import com.app.erp.user.repository.NotificationRepository;
import com.app.erp.user.repository.UserNotificationRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationCleanupService {

    private final UserNotificationRepository userNotificationRepository;
    private final NotificationRepository notificationRepository;

    public NotificationCleanupService(UserNotificationRepository userNotificationRepository,
                                      NotificationRepository notificationRepository) {
        this.userNotificationRepository = userNotificationRepository;
        this.notificationRepository = notificationRepository;
    }

    // Delete soft-deleted notification which is older than 30 days (every day in 3:00)
     @Scheduled(cron = "0 0 3 * * ?")
    //@Scheduled(cron = "*/10 * * * * ?") // every 10 seconds
    @Transactional
    public void deleteOldSoftDeletedUserNotifications() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);

        // 1) Delete UserNotification older than 30 days
        List<UserNotification> toDelete = userNotificationRepository.findAll().stream()
                .filter(un -> un.isDeleted()
                        && un.getNotification().getTimestamp().isBefore(cutoffDate))
                .toList();

        if (!toDelete.isEmpty()) {
            userNotificationRepository.deleteAll(toDelete);
            System.out.println("Deleted UserNotification: " + toDelete.size());
        }

        // 2) Delete Notification that does not have anymore UserNotification connected
        // and which is older than 60 dana (or some other period)
        LocalDateTime notificationCutoff = LocalDateTime.now().minusDays(60);
        List<Notification> notificationsToDelete = notificationRepository.findAll().stream()
                .filter(n -> n.getTimestamp().isBefore(notificationCutoff))
                .filter(n -> !userNotificationRepository.existsByNotification(n))
                .toList();

        if (!notificationsToDelete.isEmpty()) {
            notificationRepository.deleteAll(notificationsToDelete);
            System.out.println("Deleted Notification: " + notificationsToDelete.size());
        }
    }
}

