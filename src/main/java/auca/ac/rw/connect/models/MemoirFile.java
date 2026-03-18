package auca.ac.rw.connect.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Metadata for a protected project memoir PDF.
 */
@Entity
@Table(name = "memoir_files", indexes = {
        @Index(name = "idx_memoir_files_project_id", columnList = "project_id"),
        @Index(name = "idx_memoir_files_scan_status", columnList = "scan_status"),
        @Index(name = "idx_memoir_files_sha256_checksum", columnList = "sha256_checksum")
})
@AttributeOverride(name = "createdAt", column = @Column(name = "uploaded_at", nullable = false, updatable = false))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MemoirFile extends BaseEntity {

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @Column(name = "storage_path", nullable = false)
    private String storagePath;

    @Column(name = "sha256_checksum", nullable = false, unique = true)
    private String sha256Checksum;

    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes;

    @Column(name = "total_pages")
    private Integer totalPages;

    @Enumerated(EnumType.STRING)
    @Column(name = "scan_status", nullable = false)
    private MemoirScanStatus scanStatus;

    @Column(name = "mime_type", nullable = false)
    private String mimeType;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false, unique = true)
    @JsonBackReference("project-memoir-file")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Project project;

    public enum MemoirScanStatus {
        PENDING,
        CLEAN,
        INFECTED,
        FAILED
    }
}
