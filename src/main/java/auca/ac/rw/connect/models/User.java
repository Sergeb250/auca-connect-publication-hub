package auca.ac.rw.connect.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
 * Master account record for every authenticated platform user.
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_university_email", columnList = "university_email"),
        @Index(name = "idx_users_role", columnList = "role"),
        @Index(name = "idx_users_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class User extends BaseEntity {

    // Core identity
    @Column(name = "university_email", nullable = false, unique = true)
    private String universityEmail;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "password_reset_token_hash")
    private String passwordResetTokenHash;

    @Column(name = "password_reset_expires_at")
    private LocalDateTime passwordResetExpiresAt;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status;

    @Column(name = "profile_avatar_url")
    private String profileAvatarUrl;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    // Relationships
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("user-campus-profile")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private CampusProfile campusProfile;

    @Builder.Default
    @OneToMany(mappedBy = "submittedBy", fetch = FetchType.LAZY)
    @JsonManagedReference("user-projects")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Project> projects = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "submittedBy", fetch = FetchType.LAZY)
    @JsonManagedReference("user-publications")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Publication> publications = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonManagedReference("user-reservations")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Reservation> reservations = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonManagedReference("user-notifications")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Notification> notifications = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "performedBy", fetch = FetchType.LAZY)
    @JsonManagedReference("user-moderation-actions")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ModerationAction> moderationActions = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonManagedReference("user-audit-logs")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<AuditLog> auditLogs = new ArrayList<>();

    public enum UserRole {
        STUDENT,
        LECTURER,
        MODERATOR,
        ADMIN
    }

    public enum UserStatus {
        ACTIVE,
        SUSPENDED,
        PENDING_VERIFICATION
    }
}
