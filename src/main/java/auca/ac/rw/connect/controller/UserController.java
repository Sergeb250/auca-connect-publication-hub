package auca.ac.rw.connect.controller;

import auca.ac.rw.connect.dto.request.ChangePasswordRequest;
import auca.ac.rw.connect.dto.request.UpdateProfileRequest;
import auca.ac.rw.connect.dto.response.ApiResponse;
import auca.ac.rw.connect.dto.response.UserResponse;
import auca.ac.rw.connect.service.UserService;
import auca.ac.rw.connect.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile() {
        UserResponse response = userService.getMyProfile(SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(response, "Profile retrieved successfully."));
    }

    @PatchMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        UserResponse response = userService.updateMyProfile(SecurityUtils.getCurrentUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(response, "Profile updated successfully."));
    }

    @PatchMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changeMyPassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changeMyPassword(SecurityUtils.getCurrentUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully."));
    }
}
