package auca.ac.rw.connect.service;

import auca.ac.rw.connect.dto.request.ChangePasswordRequest;
import auca.ac.rw.connect.dto.request.UpdateProfileRequest;
import auca.ac.rw.connect.dto.response.UserResponse;
import java.util.UUID;

public interface UserService {

    UserResponse getMyProfile(UUID userId);

    UserResponse updateMyProfile(UUID userId, UpdateProfileRequest request);

    void changeMyPassword(UUID userId, ChangePasswordRequest request);
}
