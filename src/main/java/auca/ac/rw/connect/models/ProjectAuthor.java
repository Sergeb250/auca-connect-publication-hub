package auca.ac.rw.connect.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
 * Single ordered project author record.
 */
@Entity
@Table(name = "project_authors", indexes = {
        @Index(name = "idx_project_authors_project_id", columnList = "project_id"),
        @Index(name = "idx_project_authors_user_id", columnList = "user_id"),
        @Index(name = "idx_project_authors_author_order", columnList = "author_order")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProjectAuthor extends BaseEntity {

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email")
    private String email;

    @Column(name = "student_id")
    private String studentId;

    @Column(name = "role", nullable = false)
    private String role;

    @Builder.Default
    @Column(name = "author_order", nullable = false)
    private int authorOrder = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonBackReference("project-authors")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;
}
