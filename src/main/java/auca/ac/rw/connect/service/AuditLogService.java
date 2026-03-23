package auca.ac.rw.connect.service;

import auca.ac.rw.connect.models.AuditLog;
import auca.ac.rw.connect.models.User;
import java.util.UUID;

public interface AuditLogService {

    void log(AuditLog.AuditAction action, AuditLog.EntityType entityType, UUID entityId, UUID actorUserId,
            String ipAddress, String detailsJson);

    void logLogin(User user, String ipAddress);

    void logLoginFailed(User user, String ipAddress, String reason);

    void logLogout(User user, String ipAddress);
}
