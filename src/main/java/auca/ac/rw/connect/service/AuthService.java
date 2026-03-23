package auca.ac.rw.connect.service;

import auca.ac.rw.connect.dto.request.ForgotPasswordRequest;
import auca.ac.rw.connect.dto.request.LoginRequest;
import auca.ac.rw.connect.dto.request.RegisterRequest;
import auca.ac.rw.connect.dto.request.ResetPasswordRequest;
import auca.ac.rw.connect.dto.response.AuthResponse;
import java.util.UUID;

public interface AuthService {

    AuthResponse login(LoginRequest request, String ipAddress);

    AuthResponse register(RegisterRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void logout(UUID userId, String ipAddress);
}
