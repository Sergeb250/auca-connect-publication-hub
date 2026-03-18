package auca.ac.rw.connect.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Live secured reading session opened from a reservation.
 */
@Entity
@Table(name = "viewer_sessions", indexes = {
        @Index(name = "idx_viewer_sessions_reservation_id", columnList = "reservation_id"),
        @Index(name = "idx_viewer_sessions_user_id", columnList = "user_id"),
        @Index(name = "idx_viewer_sessions_project_id", columnList = "project_id"),
        @Index(name = "idx_viewer_sessions_session_token", columnList = "session_token"),
        @Index(name = "idx_viewer_sessions_token_expires_at", columnList = "token_expires_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ViewerSession extends BaseEntity {

    @Column(name = "session_token", nullable = false, unique = true)
    private String sessionToken;

    @Builder.Default
    @Column(name = "total_pages_viewed", nullable = false)
    private Integer totalPagesViewed = 0;

    @Builder.Default
    @Column(name = "last_page_viewed", nullable = false)
    private Integer lastPageViewed = 0;

    @Column(name = "total_pages_in_document")
    private Integer totalPagesInDocument;

    @Builder.Default
    @Column(name = "total_time_spent_seconds", nullable = false)
    private Long totalTimeSpentSeconds = 0L;

    @Builder.Default
    @Column(name = "session_active", nullable = false)
    private boolean sessionActive = false;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "token_expires_at", nullable = false)
    private LocalDateTime tokenExpiresAt;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false, unique = true)
    @JsonBackReference("reservation-viewer-session")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Project project;

    @Builder.Default
    @OneToMany(mappedBy = "viewerSession", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    @JsonManagedReference("viewer-session-page-views")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<PageViewLog> pageViewLogs = new ArrayList<>();
}
