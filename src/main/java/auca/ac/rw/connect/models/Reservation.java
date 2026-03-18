package auca.ac.rw.connect.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Booking that authorizes a controlled memoir viewing window.
 */
@Entity
@Table(name = "reservations", indexes = {
        @Index(name = "idx_reservations_user_id", columnList = "user_id"),
        @Index(name = "idx_reservations_project_id", columnList = "project_id"),
        @Index(name = "idx_reservations_status", columnList = "status"),
        @Index(name = "idx_reservations_slot_start", columnList = "slot_start"),
        @Index(name = "idx_reservations_slot_end", columnList = "slot_end")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Reservation extends BaseEntity {

    @Column(name = "slot_start", nullable = false)
    private LocalDateTime slotStart;

    @Column(name = "slot_end", nullable = false)
    private LocalDateTime slotEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReservationStatus status;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Builder.Default
    @Column(name = "renewal_requested", nullable = false)
    private boolean renewalRequested = false;

    @Builder.Default
    @Column(name = "renewal_approved", nullable = false)
    private boolean renewalApproved = false;

    @Column(name = "renewal_rejection_reason")
    private String renewalRejectionReason;

    @Builder.Default
    @Column(name = "no_show_count", nullable = false)
    private int noShowCount = 0;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference("user-reservations")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonBackReference("project-reservations")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Project project;

    @OneToOne(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("reservation-viewer-session")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ViewerSession viewerSession;

    public enum ReservationStatus {
        PENDING,
        CONFIRMED,
        ACTIVE,
        COMPLETED,
        CANCELLED,
        NO_SHOW,
        RENEWAL_REQUESTED
    }
}
