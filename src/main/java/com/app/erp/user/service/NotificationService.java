    package com.app.erp.user.service;
    
    import com.app.erp.dto.notification.NotificationDTO;
    import com.app.erp.entity.notification.Notification;
    import com.app.erp.entity.notification.UserNotification;
    import com.app.erp.entity.user.Role;
    import com.app.erp.entity.user.User;
    import com.app.erp.goods.exceptions.NotFoundException;
    import com.app.erp.goods.exceptions.NotificationAccessDeniedException;
    import com.app.erp.security.UserDetails;
    import com.app.erp.user.repository.NotificationRepository;
    import com.app.erp.user.repository.UserNotificationRepository;
    import com.app.erp.user.repository.UserRepository;
    import jakarta.transaction.Transactional;
    import org.springframework.messaging.simp.SimpMessagingTemplate;
    import org.springframework.stereotype.Service;
    
    import java.time.LocalDateTime;
    import java.util.*;
    import java.util.stream.Collectors;
    
    @Service
    public class NotificationService {
    
        private final NotificationRepository notificationRepository;
        private final UserRepository userRepository;
        private final UserNotificationRepository userNotificationRepository;
        private final SimpMessagingTemplate messagingTemplate;
    
        public NotificationService(NotificationRepository notificationRepository,
                                   UserRepository userRepository,
                                   UserNotificationRepository userNotificationRepository,
                                   SimpMessagingTemplate messagingTemplate) {
            this.notificationRepository = notificationRepository;
            this.userRepository = userRepository;
            this.userNotificationRepository = userNotificationRepository;
            this.messagingTemplate = messagingTemplate;
        }
    
        @Transactional
        public void createAndSendNotification(String type, String content, List<String> roles) {
            Notification notification = new Notification();
            notification.setType(type);
            notification.setContent(content);
            notification.setRecipientRoles(roles);
            notification.setTimestamp(LocalDateTime.now());
    
            Notification savedNotification = notificationRepository.save(notification);
            messagingTemplate.convertAndSend("/topic/notifications", convertToDTO(savedNotification, false));
            // Send real-time update via WebSocket
            messagingTemplate.convertAndSend("/topic/notifications",
                    Map.of(
                            "type", type,
                            "content", content,
                            "timestamp", LocalDateTime.now().toString()
                    )
            );
        }
    
        @Transactional
        public List<NotificationDTO> getUserNotifications(UserDetails userDetails) {
            User user = userRepository.findByEmail(userDetails.getUsername());
            List<String> userRoles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());
    
            // Get all notifications for the user's roles
            List<Notification> notifications = notificationRepository.findByRecipientRolesIn(userRoles);
    
            // Get user notifications for the notifications
            List<UserNotification> userNotifications = userNotificationRepository.findByUserAndNotificationIn(user, notifications);
    
            // Map user notifications to read and deleted status
            Map<Long, Boolean> readStatusMap = new HashMap<>();
            Map<Long, Boolean> deletedStatusMap = new HashMap<>();
    
            userNotifications.forEach(un -> {
                readStatusMap.put(un.getNotification().getId(), un.isRead());
                deletedStatusMap.put(un.getNotification().getId(), un.isDeleted());
            });
    
            // Filter notifications based on read and deleted status
            return notifications.stream()
                    .filter(notification -> !deletedStatusMap.getOrDefault(notification.getId(), false))
                    .map(notification -> convertToDTO(
                            notification,
                            readStatusMap.getOrDefault(notification.getId(), false)
                    ))
                    .collect(Collectors.toList());
        }
    
        @Transactional
        public void markAsRead(Long notificationId, UserDetails userDetails) {
            User user = userRepository.findByEmail(userDetails.getUsername());
            Notification notification = notificationRepository.findById(notificationId)
                    .orElseThrow(() -> new NotFoundException("Notification not found"));
    
            checkAccess(notification, user);
    
            UserNotification userNotification = userNotificationRepository
                    .findByUserAndNotification(user, notification)
                    .orElseGet(() -> createUserNotification(user, notification, false));
    
            if (!userNotification.isRead()) {
                userNotification.setRead(true);
                userNotificationRepository.save(userNotification);
            }
        }
    
        @Transactional
        public void markAllAsRead(UserDetails userDetails) {
            User user = userRepository.findByEmail(userDetails.getUsername());
            List<String> userRoles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());
    
            List<Notification> notifications = notificationRepository.findByRecipientRolesIn(userRoles);
            List<UserNotification> userNotifications = userNotificationRepository.findByUserAndNotificationIn(user, notifications);
    
            // Use LinkedHashMap and merge duplicates
            Map<Long, UserNotification> userNotificationMap = userNotifications.stream()
                    .collect(Collectors.toMap(
                            un -> un.getNotification().getId(),
                            un -> un,
                            (existing, replacement) -> existing,
                            LinkedHashMap::new
                    ));
    
            notifications.forEach(notification -> {
                UserNotification un = userNotificationMap.computeIfAbsent(notification.getId(),
                        k -> createUserNotification(user, notification, false));
    
                if (!un.isRead()) {
                    un.setRead(true);
                    userNotificationRepository.save(un);
                }
            });
        }
    
        @Transactional
        public void deleteNotification(Long notificationId, UserDetails userDetails) {
            User user = userRepository.findByEmail(userDetails.getUsername());
            Notification notification = notificationRepository.findById(notificationId)
                    .orElseThrow(() -> new NotFoundException("Notification not found"));
    
            checkAccess(notification, user);
    
            UserNotification userNotification = userNotificationRepository
                    .findByUserAndNotification(user, notification)
                    .orElseGet(() -> createUserNotification(user, notification, true));
    
            if (!userNotification.isDeleted()) {
                userNotification.setDeleted(true);
                userNotificationRepository.save(userNotification);
            }
        }

        @Transactional
        public void deleteAllNotifications(UserDetails userDetails) {
            User user = userRepository.findByEmail(userDetails.getUsername());

            // Pronađi sve notifikacije koje je ovaj korisnik dobio
            List<UserNotification> userNotifications = userNotificationRepository.findByUser(user);

            // Označi ih kao obrisane (ne briši fizički!)
            for (UserNotification un : userNotifications) {
                if (!un.isDeleted()) {
                    un.setDeleted(true);
                    userNotificationRepository.save(un);
                }
            }
        }


        private UserNotification createUserNotification(User user, Notification notification, boolean deleted) {
            UserNotification un = new UserNotification();
            un.setUser(user);
            un.setNotification(notification);
            un.setRead(false);
            un.setDeleted(deleted);
            return userNotificationRepository.save(un);
        }
    
        private void checkAccess(Notification notification, User user) {
            List<String> userRoles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());
    
            if (notification.getRecipientRoles().stream().noneMatch(userRoles::contains)) {
                throw new NotificationAccessDeniedException("Access denied");
            }
        }
    
        private NotificationDTO convertToDTO(Notification notification, boolean isRead) {
            return new NotificationDTO(
                    notification.getId(),
                    notification.getType(),
                    notification.getContent(),
                    notification.getRecipientRoles(),
                    notification.getTimestamp(),
                    isRead
            );
        }
    }
