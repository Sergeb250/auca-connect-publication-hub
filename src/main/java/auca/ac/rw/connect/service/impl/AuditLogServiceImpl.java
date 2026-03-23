package auca.ac.rw.connect.service.impl;

import auca.ac.rw.connect.models.AuditLog;
import auca.ac.rw.connect.models.User;
import auca.ac.rw.connect.repository.AuditLogRepository;
import auca.ac.rw.connect.repository.UserRepository;
import auca.ac.rw.connect.service.AuditLogService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Async
    @PreAuthorize("permitAll()")
    @Transactional
    public void log(AuditLog.AuditAction action, AuditLog.EntityType entityType, UUID entityId, UUID actorUserId,
            String ipAddress, String detailsJson) {
        doLog(action, entityType, entityId, actorUserId, ipAddress, detailsJson);
    }

    @Override
    @Async
    @PreAuthorize("permitAll()")
    @Transactional
    public void logLogin(User user, String ipAddress) {
        doLog(AuditLog.AuditAction.LOGIN, AuditLog.EntityType.USER, user.getId(), user.getId(), ipAddress, null);
    }

    @Override
    @Async
    @PreAuthorize("permitAll()")
    @Transactional
    public void logLoginFailed(User user, String ipAddress, String reason) {
        doLog(
                AuditLog.AuditAction.LOGIN_FAILED,
                AuditLog.EntityType.USER,
                user != null ? user.getId() : null,
                user != null ? user.getId() : null,
                ipAddress,
                toJson(Map.of("reason", reason)));
    }

    @Override
    @Async
    @PreAuthorize("permitAll()")
    @Transactional
    public void logLogout(User user, String ipAddress) {
        doLog(AuditLog.AuditAction.LOGOUT, AuditLog.EntityType.USER, user.getId(), user.getId(), ipAddress, null);
    }

    private void doLog(AuditLog.AuditAction action, AuditLog.EntityType entityType, UUID entityId, UUID actorUserId,
            String ipAddress, String detailsJson) {
        try {
            User actor = actorUserId == null ? null : userRepository.findById(actorUserId).orElse(null);

            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .detailsJson(detailsJson)
                    .ipAddress(ipAddress)
                    .user(actor)
                    .build();

            auditLogRepository.save(auditLog);
            log.info("Audit log saved for action {}", action);
        } catch (Exception exception) {
            log.error("Failed to persist audit log for action {}", action, exception);
        }
    }

    private String toJson(Map<String, String> details) {
        try {
            return objectMapper.writeValueAsString(details);
        } catch (JsonProcessingException exception) {
            log.warn("Failed to serialize audit log details to JSON.", exception);
            return "{\"reason\":\"UNKNOWN\"}";
        }
    }
}
