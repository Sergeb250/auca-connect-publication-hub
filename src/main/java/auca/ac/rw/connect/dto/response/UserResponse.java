package auca.ac.rw.connect.dto.response;

import auca.ac.rw.connect.models.User;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private User.UserRole role;
    private User.UserStatus status;
    private String profileAvatarUrl;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private CampusProfileResponse campusProfile;

    public static UserResponse from(User user) {
        if (user == null) {
            return null;
        }

        return UserResponse.builder()
                .userId(user.getId())
                .email(user.getUniversityEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .status(user.getStatus())
                .profileAvatarUrl(user.getProfileAvatarUrl())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .campusProfile(CampusProfileResponse.from(user.getCampusProfile()))
                .build();
    }
}
