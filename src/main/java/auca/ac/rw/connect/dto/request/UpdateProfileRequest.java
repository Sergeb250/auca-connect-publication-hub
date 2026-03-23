package auca.ac.rw.connect.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @Size(max = 100, message = "First name must not exceed 100 characters.")
    private String firstName;

    @Size(max = 100, message = "Last name must not exceed 100 characters.")
    private String lastName;

    private String phoneNumber;

    @Pattern(regexp = "^(https?://).+$", message = "Profile avatar URL must be a valid URL.")
    private String profileAvatarUrl;
}
