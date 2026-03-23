package auca.ac.rw.connect.service.impl;

import auca.ac.rw.connect.dto.request.ChangePasswordRequest;
import auca.ac.rw.connect.dto.request.UpdateProfileRequest;
import auca.ac.rw.connect.dto.response.UserResponse;
import auca.ac.rw.connect.exception.AucaException;
import auca.ac.rw.connect.exception.ErrorCode;
import auca.ac.rw.connect.models.AuditLog;
import auca.ac.rw.connect.models.User;
import auca.ac.rw.connect.repository.UserRepository;
import auca.ac.rw.connect.service.AuditLogService;
import auca.ac.rw.connect.service.UserService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    @Override
    @PreAuthorize("hasAnyRole('STUDENT','LECTURER','MODERATOR','ADMIN')")
    @Transactional(readOnly = true)
    public UserResponse getMyProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AucaException(ErrorCode.ERR_NOT_FOUND, "User not found."));
        return UserResponse.from(user);
    }

    @Override
    @PreAuthorize("hasAnyRole('STUDENT','LECTURER','MODERATOR','ADMIN')")
    @Transactional
    public UserResponse updateMyProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AucaException(ErrorCode.ERR_NOT_FOUND, "User not found."));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName().trim());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber().trim());
        }
        if (request.getProfileAvatarUrl() != null) {
            user.setProfileAvatarUrl(request.getProfileAvatarUrl().trim());
        }

        User savedUser = userRepository.save(user);
        log.info("Profile updated for user {}", savedUser.getUniversityEmail());
        return UserResponse.from(savedUser);
    }

    @Override
    @PreAuthorize("hasAnyRole('STUDENT','LECTURER','MODERATOR','ADMIN')")
    @Transactional
    public void changeMyPassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AucaException(ErrorCode.ERR_NOT_FOUND, "User not found."));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new AucaException(ErrorCode.ERR_AUTH_401, "Current password is incorrect.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        auditLogService.log(
                AuditLog.AuditAction.PASSWORD_CHANGED,
                AuditLog.EntityType.USER,
                user.getId(),
                user.getId(),
                null,
                null);

        log.info("Password changed for user {}", user.getUniversityEmail());
    }
}
