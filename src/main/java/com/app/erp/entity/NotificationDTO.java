package com.app.erp.entity;

import java.time.LocalDateTime;
import java.util.List;

public record NotificationDTO(
        Long id,
        String type,
        String content,
        List<String> recipientRoles,
        LocalDateTime timestamp,
        Boolean isRead
) {}
