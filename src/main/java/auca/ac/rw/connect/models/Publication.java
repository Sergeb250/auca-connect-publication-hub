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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.LocalDate;
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
 * Lecturer-submitted publication record.
 */
@Entity
@Table(name = "publications", indexes = {
        @Index(name = "idx_publications_submitted_by", columnList = "submitted_by"),
        @Index(name = "idx_publications_department", columnList = "department"),
        @Index(name = "idx_publications_academic_year", columnList = "academic_year"),
        @Index(name = "idx_publications_type", columnList = "type"),
        @Index(name = "idx_publications_status", columnList = "status"),
        @Index(name = "idx_publications_visibility", columnList = "visibility"),
        @Index(name = "idx_publications_published_at", columnList = "published_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Publication extends BaseEntity {

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "abstract_text", nullable = false, columnDefinition = "TEXT")
    private String abstractText;

    @Column(name = "keywords", nullable = false, columnDefinition = "TEXT")
    private String keywords;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private PublicationType type;

    @Column(name = "department", nullable = false)
    private String department;

    @Column(name = "academic_year", nullable = false)
    private String academicYear;

    @Column(name = "doi_or_external_link")
    private String doiOrExternalLink;

    @Column(name = "journal_or_conference_name")
    private String journalOrConferenceName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PublicationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false)
    private Project.VisibilityLevel visibility;

    @Column(name = "version_label")
    private String versionLabel;

    @Column(name = "access_notes", columnDefinition = "TEXT")
    private String accessNotes;

    @Builder.Default
    @Column(name = "notify_co_authors", nullable = false)
    private boolean notifyCoAuthors = false;

    @Column(name = "embargo_until")
    private LocalDate embargoUntil;

    @Builder.Default
    @Column(name = "view_count", nullable = false)
    private int viewCount = 0;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by", nullable = false)
    @JsonBackReference("user-publications")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User submittedBy;

    @Builder.Default
    @OneToMany(mappedBy = "publication", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("authorOrder ASC")
    @JsonManagedReference("publication-authors")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<PublicationAuthor> authors = new ArrayList<>();

    @OneToOne(mappedBy = "publication", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("publication-file")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private PublicationFile publicationFile;

    @Builder.Default
    @OneToMany(mappedBy = "publication", fetch = FetchType.LAZY)
    @JsonManagedReference("publication-moderation-actions")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ModerationAction> moderationActions = new ArrayList<>();

    public enum PublicationType {
        RESEARCH_PAPER,
        JOURNAL_ARTICLE,
        CONFERENCE_PAPER,
        BOOK_CHAPTER,
        TECHNICAL_REPORT,
        OTHER
    }

    public enum PublicationStatus {
        DRAFT,
        PENDING_APPROVAL,
        PUBLISHED,
        REJECTED,
        HIDDEN,
        ARCHIVED
    }
}
