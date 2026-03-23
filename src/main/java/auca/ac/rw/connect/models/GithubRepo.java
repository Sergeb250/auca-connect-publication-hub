package auca.ac.rw.connect.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
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
 * Cached GitHub repository metadata for a project.
 */
@Entity
@Table(name = "github_repos", indexes = {
        @Index(name = "idx_github_repos_project_id", columnList = "project_id"),
        @Index(name = "idx_github_repos_repo_owner", columnList = "repo_owner"),
        @Index(name = "idx_github_repos_repo_name", columnList = "repo_name")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GithubRepo extends BaseEntity {

    @Column(name = "repo_url", nullable = false)
    private String repoUrl;

    @Column(name = "repo_name", nullable = false)
    private String repoName;

    @Column(name = "repo_owner", nullable = false)
    private String repoOwner;

    @Column(name = "default_branch", nullable = false)
    private String defaultBranch;

    @Column(name = "final_commit_hash", nullable = false)
    private String finalCommitHash;

    @Column(name = "final_commit_message")
    private String finalCommitMessage;

    @Column(name = "final_tag")
    private String finalTag;

    @Column(name = "readme_content", columnDefinition = "TEXT")
    private String readmeContent;

    @Column(name = "source_tree_json", columnDefinition = "TEXT")
    private String sourceTreeJson;

    @Column(name = "repository_snapshot_path")
    private String repositorySnapshotPath;

    @Column(name = "primary_language")
    private String primaryLanguage;

    @Builder.Default
    @Column(name = "star_count", nullable = false)
    private Integer starCount = 0;

    @Builder.Default
    @Column(name = "fork_count", nullable = false)
    private Integer forkCount = 0;

    @Builder.Default
    @Column(name = "is_private", nullable = false)
    private Boolean isPrivate = false;

    @Builder.Default
    @Column(name = "embedded_view_enabled", nullable = false)
    private Boolean embeddedViewEnabled = true;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    @Column(name = "last_source_indexed_at")
    private LocalDateTime lastSourceIndexedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false, unique = true)
    @JsonBackReference("project-github-repo")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Project project;
}
