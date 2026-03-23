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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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
 * Primary student-submitted scholarly work record.
 */
@Entity
@Table(name = "projects", indexes = {
        @Index(name = "idx_projects_submitted_by", columnList = "submitted_by"),
        @Index(name = "idx_projects_department", columnList = "department"),
        @Index(name = "idx_projects_academic_year", columnList = "academic_year"),
        @Index(name = "idx_projects_category", columnList = "category"),
        @Index(name = "idx_projects_status", columnList = "status"),
        @Index(name = "idx_projects_type", columnList = "type"),
        @Index(name = "idx_projects_visibility", columnList = "visibility"),
        @Index(name = "idx_projects_published_at", columnList = "published_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Project extends BaseEntity {

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "abstract_text", nullable = false, columnDefinition = "TEXT")
    private String abstractText;

    @Column(name = "keywords", nullable = false, columnDefinition = "TEXT")
    private String keywords;

    @Column(name = "department", nullable = false)
    private String department;

    @Column(name = "academic_year", nullable = false)
    private String academicYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private ProjectCategory category;

    @Column(name = "technologies_used", columnDefinition = "TEXT")
    private String technologiesUsed;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProjectStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ProjectType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false)
    private VisibilityLevel visibility;

    @Column(name = "supervisor_name")
    private String supervisorName;

    @Column(name = "embargo_until")
    private LocalDate embargoUntil;

    @Builder.Default
    @Column(name = "view_count", nullable = false)
    private int viewCount = 0;

    @Builder.Default
    @Column(name = "reservation_count", nullable = false)
    private int reservationCount = 0;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by", nullable = false)
    @JsonBackReference("user-projects")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User submittedBy;

    @Builder.Default
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("authorOrder ASC")
    @JsonManagedReference("project-authors")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ProjectAuthor> authors = new ArrayList<>();

    @OneToOne(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("project-memoir-file")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private MemoirFile memoirFile;

    @OneToOne(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("project-github-repo")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private GithubRepo githubRepo;

    @Builder.Default
    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY)
    @JsonManagedReference("project-reservations")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Reservation> reservations = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY)
    @JsonManagedReference("project-moderation-actions")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ModerationAction> moderationActions = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY)
    @OrderBy("positionInQueue ASC, createdAt ASC")
    @JsonManagedReference("project-waitlist")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Waitlist> waitlistEntries = new ArrayList<>();

    @PrePersist
    @PreUpdate
    void validateProjectOwner() {
        if (submittedBy == null || submittedBy.getRole() == null) {
            return;
        }

        if (submittedBy.getRole() != User.UserRole.STUDENT) {
            throw new IllegalStateException("Projects can only be submitted by users with the STUDENT role.");
        }
    }

    public enum ProjectCategory {
        SOFTWARE_SYSTEM,
        RESEARCH_STUDY,
        DATA_ANALYSIS,
        MOBILE_APP,
        WEB_APP,
        OTHER
    }

    public enum ProjectStatus {
        DRAFT,
        PENDING_APPROVAL,
        PUBLISHED,
        REJECTED,
        HIDDEN,
        ARCHIVED
    }

    public enum ProjectType {
        FINAL_YEAR_PROJECT,
        POSTGRADUATE_THESIS,
        COURSEWORK
    }

    public enum VisibilityLevel {
        AUCA_ONLY,
        RESTRICTED,
        EMBARGOED
    }
}
