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
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Immutable moderation decision on a project or publication.
 */
@Entity
@Table(name = "moderation_actions", indexes = {
        @Index(name = "idx_moderation_actions_performed_by", columnList = "performed_by"),
        @Index(name = "idx_moderation_actions_project_id", columnList = "project_id"),
        @Index(name = "idx_moderation_actions_publication_id", columnList = "publication_id"),
        @Index(name = "idx_moderation_actions_item_type_item_id", columnList = "item_type, item_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ModerationAction extends AppendOnlyBaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private ModerationActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private ItemType itemType;

    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "duplicate_of_id")
    private String duplicateOfId;

    @Column(name = "rejection_template_name")
    private String rejectionTemplateName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by", nullable = false)
    @JsonBackReference("user-moderation-actions")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User performedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    @JsonBackReference("project-moderation-actions")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publication_id")
    @JsonBackReference("publication-moderation-actions")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Publication publication;

    public enum ModerationActionType {
        APPROVE,
        REJECT,
        HIDE,
        UNPUBLISH,
        REQUEST_REUPLOAD,
        MARK_DUPLICATE,
        ARCHIVE,
        RESTORE,
        RENEWAL_APPROVED,
        RENEWAL_REJECTED
    }

    public enum ItemType {
        PROJECT,
        PUBLICATION
    }
}
