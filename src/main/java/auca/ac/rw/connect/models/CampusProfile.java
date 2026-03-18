package auca.ac.rw.connect.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Verified academic identity linked to a user account.
 */
@Entity
@Table(name = "campus_profiles", indexes = {
        @Index(name = "idx_campus_profiles_campus_id", columnList = "campus_id"),
        @Index(name = "idx_campus_profiles_department", columnList = "department"),
        @Index(name = "idx_campus_profiles_user_id", columnList = "user_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CampusProfile extends BaseEntity {

    @Column(name = "campus_id", nullable = false, unique = true)
    private String campusId;

    @Column(name = "faculty", nullable = false)
    private String faculty;

    @Column(name = "department", nullable = false)
    private String department;

    @Column(name = "academic_year")
    private String academicYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "academic_level")
    private AcademicLevel academicLevel;

    @Column(name = "supervisor_name")
    private String supervisorName;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonBackReference("user-campus-profile")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    public enum AcademicLevel {
        UNDERGRADUATE,
        POSTGRADUATE,
        STAFF,
        LECTURER
    }
}
