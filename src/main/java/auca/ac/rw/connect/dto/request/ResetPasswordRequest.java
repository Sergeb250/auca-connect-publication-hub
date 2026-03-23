package auca.ac.rw.connect.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {

    @NotBlank(message = "Reset token is required.")
    private String resetToken;

    @NotBlank(message = "New password is required.")
    @Size(min = 8, message = "New password must be at least 8 characters.")
    private String newPassword;

    @NotBlank(message = "Confirm new password is required.")
    private String confirmNewPassword;

    @AssertTrue(message = "Passwords do not match")
    public boolean isPasswordsMatching() {
        return newPassword != null && newPassword.equals(confirmNewPassword);
    }
}
