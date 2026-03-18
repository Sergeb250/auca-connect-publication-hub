package auca.ac.rw.connect.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * In-app notification delivered to a user.
 */
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notifications_user_id", columnList = "user_id"),
        @Index(name = "idx_notifications_notification_type", columnList = "notification_type"),
        @Index(name = "idx_notifications_related_entity", columnList = "related_entity_type, related_entity_id"),
        @Index(name = "idx_notifications_is_read", columnList = "is_read")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Notification extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType notificationType;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Builder.Default
    @Column(name = "email_sent", nullable = false)
    private boolean emailSent = false;

    @Column(name = "related_entity_id")
    private UUID relatedEntityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "related_entity_type")
    private AuditLog.EntityType relatedEntityType;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference("user-notifications")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    public enum NotificationType {
        PROJECT_APPROVED,
        PROJECT_REJECTED,
        PROJECT_REUPLOAD_REQUESTED,
        RESERVATION_CONFIRMED,
        RESERVATION_REMINDER,
        RESERVATION_EXPIRED,
        RENEWAL_APPROVED,
        RENEWAL_REJECTED,
        PUBLICATION_APPROVED,
        PUBLICATION_REJECTED,
        SYSTEM_ANNOUNCEMENT
    }
}
