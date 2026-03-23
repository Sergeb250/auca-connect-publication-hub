package auca.ac.rw.connect.service;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public interface CampusVerificationService {

    CampusData verify(String email, String campusId);

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class CampusData {
        @NotBlank
        private String campusId;
        private String firstName;
        private String lastName;
        private String faculty;
        private String department;
        private String academicYear;
        private String academicLevel;
        private boolean enrolled;
        private String supervisorName;
    }
}
