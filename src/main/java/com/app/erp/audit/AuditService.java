package com.app.erp.audit;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuditService {

        private final KafkaTemplate<String, Object> kafkaTemplate;

        public AuditService(KafkaTemplate<String, Object> kafkaTemplate) {
            this.kafkaTemplate = kafkaTemplate;
        }

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public void logEvent(String action, String entity, Long entityId, Map<String, Object> details) {
        String username = getCurrentUsername();
        Map<String, Object> auditEvent = new HashMap<>();
        auditEvent.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        auditEvent.put("action", action);
        auditEvent.put("entity", entity);
        auditEvent.put("entityId", entityId);
        auditEvent.put("user", username);

        if (details != null) {
            auditEvent.putAll(details);
        }

        kafkaTemplate.send("audit-log", auditEvent);
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "system";
    }
}
