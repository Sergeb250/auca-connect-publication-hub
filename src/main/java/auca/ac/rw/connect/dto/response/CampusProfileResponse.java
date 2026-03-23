package auca.ac.rw.connect.dto.response;

import auca.ac.rw.connect.models.CampusProfile;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampusProfileResponse {

    private String campusId;
    private String faculty;
    private String department;
    private String academicYear;
    private CampusProfile.AcademicLevel academicLevel;
    private String supervisorName;
    private LocalDateTime verifiedAt;

    public static CampusProfileResponse from(CampusProfile profile) {
        if (profile == null) {
            return null;
        }

        return CampusProfileResponse.builder()
                .campusId(profile.getCampusId())
                .faculty(profile.getFaculty())
                .department(profile.getDepartment())
                .academicYear(profile.getAcademicYear())
                .academicLevel(profile.getAcademicLevel())
                .supervisorName(profile.getSupervisorName())
                .verifiedAt(profile.getVerifiedAt())
                .build();
    }
}
