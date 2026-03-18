package auca.ac.rw.connect.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Immutable record of one viewed memoir page.
 */
@Entity
@Table(name = "page_view_logs", indexes = {
        @Index(name = "idx_page_view_logs_viewer_session_id", columnList = "viewer_session_id"),
        @Index(name = "idx_page_view_logs_page_number", columnList = "page_number")
})
@AttributeOverride(name = "createdAt", column = @Column(name = "viewed_at", nullable = false, updatable = false))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PageViewLog extends AppendOnlyBaseEntity {

    @Column(name = "page_number", nullable = false)
    private Integer pageNumber;

    @Builder.Default
    @Column(name = "time_spent_seconds", nullable = false)
    private Long timeSpentSeconds = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viewer_session_id", nullable = false)
    @JsonBackReference("viewer-session-page-views")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ViewerSession viewerSession;
}
