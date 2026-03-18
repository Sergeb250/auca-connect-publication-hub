package auca.ac.rw.connect.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Immutable system-wide audit trail record.
 */
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_logs_user_id", columnList = "user_id"),
        @Index(name = "idx_audit_logs_action", columnList = "action"),
        @Index(name = "idx_audit_logs_entity_type_entity_id", columnList = "entity_type, entity_id")
})
@AttributeOverride(name = "createdAt", column = @Column(name = "timestamp", nullable = false, updatable = false))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AuditLog extends AppendOnlyBaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private AuditAction action;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    private EntityType entityType;

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(name = "details_json", columnDefinition = "TEXT")
    private String detailsJson;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonBackReference("user-audit-logs")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    public enum AuditAction {
        LOGIN,
        LOGOUT,
        LOGIN_FAILED,
        PROJECT_SUBMITTED,
        PROJECT_PUBLISHED,
        PROJECT_REJECTED,
        PROJECT_VIEWED,
        MEMOIR_VIEWED,
        RESERVATION_CREATED,
        RESERVATION_CANCELLED,
        RESERVATION_NO_SHOW,
        PUBLICATION_SUBMITTED,
        PUBLICATION_PUBLISHED,
        USER_SUSPENDED,
        USER_ROLE_CHANGED,
        SETTINGS_CHANGED,
        BACKUP_TRIGGERED,
        FILE_UPLOADED
    }

    public enum EntityType {
        USER,
        PROJECT,
        PUBLICATION,
        RESERVATION,
        VIEWER_SESSION,
        SYSTEM
    }
}
