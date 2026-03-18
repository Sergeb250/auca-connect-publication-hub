package auca.ac.rw.connect.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Queue entry for users waiting on a memoir reservation slot.
 */
@Entity
@Table(name = "waitlist", indexes = {
        @Index(name = "idx_waitlist_user_id", columnList = "user_id"),
        @Index(name = "idx_waitlist_project_id", columnList = "project_id"),
        @Index(name = "idx_waitlist_status", columnList = "status"),
        @Index(name = "idx_waitlist_position_in_queue", columnList = "position_in_queue")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Waitlist extends BaseEntity {

    @Column(name = "position_in_queue", nullable = false)
    private Integer positionInQueue;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WaitlistStatus status;

    @Column(name = "notified_at")
    private LocalDateTime notifiedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonBackReference("project-waitlist")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Project project;

    public enum WaitlistStatus {
        WAITING,
        NOTIFIED,
        CONVERTED_TO_RESERVATION,
        EXPIRED,
        CANCELLED
    }
}
