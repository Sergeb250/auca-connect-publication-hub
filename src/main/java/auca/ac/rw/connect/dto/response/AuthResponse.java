package auca.ac.rw.connect.dto.response;

import auca.ac.rw.connect.models.User;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private UUID userId;
    private String token;
    private String email;
    private String firstName;
    private String lastName;
    private User.UserRole role;
    private String department;
    private String academicYear;
    private String faculty;
}
