package auca.ac.rw.connect.service.impl;

import auca.ac.rw.connect.dto.request.ForgotPasswordRequest;
import auca.ac.rw.connect.dto.request.LoginRequest;
import auca.ac.rw.connect.dto.request.RegisterRequest;
import auca.ac.rw.connect.dto.request.ResetPasswordRequest;
import auca.ac.rw.connect.dto.response.AuthResponse;
import auca.ac.rw.connect.exception.AucaException;
import auca.ac.rw.connect.exception.ErrorCode;
import auca.ac.rw.connect.models.AuditLog;
import auca.ac.rw.connect.models.CampusProfile;
import auca.ac.rw.connect.models.User;
import auca.ac.rw.connect.repository.CampusProfileRepository;
import auca.ac.rw.connect.repository.UserRepository;
import auca.ac.rw.connect.security.JwtTokenProvider;
import auca.ac.rw.connect.service.AuditLogService;
import auca.ac.rw.connect.service.AuthService;
import auca.ac.rw.connect.service.CampusVerificationService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final CampusProfileRepository campusProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final CampusVerificationService campusVerificationService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuditLogService auditLogService;
    private final JavaMailSender mailSender;

    @Value("${app.auth.password-reset-url:http://localhost:3000/reset-password}")
    private String passwordResetUrl;

    @Value("${app.auth.password-reset-token-minutes:10}")
    private long passwordResetTokenMinutes;

    @Value("${spring.mail.username:}")
    private String mailFromAddress;

    @Override
    @PreAuthorize("permitAll()")
    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress) {
        String normalizedEmail = normalizeEmail(request.getEmail());
        User user = userRepository.findByUniversityEmail(normalizedEmail)
                .orElseThrow(() -> new AucaException(ErrorCode.ERR_VER_404, "Account not found in the system."));

        if (user.getStatus() == User.UserStatus.SUSPENDED) {
            auditLogService.logLoginFailed(user, ipAddress, "ACCOUNT_SUSPENDED");
            throw new AucaException(ErrorCode.ERR_AUTH_403, "This account is suspended.");
        }

        if (user.getStatus() == User.UserStatus.PENDING_VERIFICATION) {
            throw new AucaException(ErrorCode.ERR_AUTH_202, "This account is pending campus verification.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            auditLogService.logLoginFailed(user, ipAddress, "WRONG_PASSWORD");
            throw new AucaException(ErrorCode.ERR_AUTH_401, "Incorrect email or password.");
        }

        String token = jwtTokenProvider.generateLoginToken(user);
        user.setLastLoginAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);
        auditLogService.logLogin(savedUser, ipAddress);

        log.info("User {} logged in successfully.", savedUser.getUniversityEmail());
        return toAuthResponse(savedUser, token);
    }

    @Override
    @PreAuthorize("permitAll()")
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());
        String normalizedCampusId = request.getCampusId().trim();

        if (userRepository.existsByUniversityEmail(normalizedEmail)) {
            throw new AucaException(ErrorCode.ERR_AUTH_409, "This email is already registered.");
        }

        CampusVerificationService.CampusData campusData =
                campusVerificationService.verify(normalizedEmail, normalizedCampusId);

        User user = User.builder()
                .universityEmail(normalizedEmail)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .role(User.UserRole.STUDENT)
                .status(User.UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);

        CampusProfile campusProfile = CampusProfile.builder()
                .user(savedUser)
                .campusId(campusData.getCampusId())
                .faculty(null)
                .department(null)
                .academicYear(null)
                .academicLevel(CampusProfile.AcademicLevel.valueOf(campusData.getAcademicLevel()))
                .supervisorName(campusData.getSupervisorName())
                .verifiedAt(LocalDateTime.now())
                .build();

        CampusProfile savedCampusProfile = campusProfileRepository.save(campusProfile);
        savedUser.setCampusProfile(savedCampusProfile);

        String token = jwtTokenProvider.generateLoginToken(savedUser);
        log.info("Student account registered successfully for {}", savedUser.getUniversityEmail());
        return toAuthResponse(savedUser, token);
    }

    @Override
    @PreAuthorize("permitAll()")
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());
        userRepository.findByUniversityEmail(normalizedEmail)
                .ifPresent(this::generateResetTokenAndSendEmail);
    }

    @Override
    @PreAuthorize("permitAll()")
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String resetToken = request.getResetToken().trim();
        User user = userRepository.findByPasswordResetTokenHash(hashToken(resetToken))
                .orElseThrow(() -> new AucaException(
                        ErrorCode.ERR_AUTH_400,
                        "Reset token is invalid or has expired."));

        if (user.getPasswordResetExpiresAt() == null || user.getPasswordResetExpiresAt().isBefore(LocalDateTime.now())) {
            clearPasswordResetToken(user);
            userRepository.save(user);
            throw new AucaException(ErrorCode.ERR_AUTH_400, "Reset token is invalid or has expired.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        clearPasswordResetToken(user);
        userRepository.save(user);
        auditLogService.log(
                AuditLog.AuditAction.PASSWORD_CHANGED,
                AuditLog.EntityType.USER,
                user.getId(),
                user.getId(),
                null,
                "{\"reason\":\"PASSWORD_RESET_TOKEN\"}");
        log.info("Password reset completed for user {}", user.getUniversityEmail());
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public void logout(UUID userId, String ipAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AucaException(ErrorCode.ERR_NOT_FOUND, "User not found."));
        auditLogService.logLogout(user, ipAddress);
        log.info("User {} logged out.", user.getUniversityEmail());
    }

    private AuthResponse toAuthResponse(User user, String token) {
        CampusProfile campusProfile = user.getCampusProfile();
        return AuthResponse.builder()
                .userId(user.getId())
                .token(token)
                .email(user.getUniversityEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .department(campusProfile != null ? campusProfile.getDepartment() : null)
                .academicYear(campusProfile != null ? campusProfile.getAcademicYear() : null)
                .faculty(campusProfile != null ? campusProfile.getFaculty() : null)
                .build();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void generateResetTokenAndSendEmail(User user) {
        String rawToken = generateSecureToken();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(passwordResetTokenMinutes);

        user.setPasswordResetTokenHash(hashToken(rawToken));
        user.setPasswordResetExpiresAt(expiry);
        userRepository.save(user);

        try {
            sendPasswordResetEmail(user, rawToken, expiry);
            log.info("Password reset email sent to {}", user.getUniversityEmail());
        } catch (MailException exception) {
            log.error("Failed to send password reset email to {}", user.getUniversityEmail(), exception);
            throw new AucaException(ErrorCode.ERR_SERVER, "Unable to send password reset email at the moment.");
        }
    }

    private void sendPasswordResetEmail(User user, String rawToken, LocalDateTime expiry) {
        String resetLink = UriComponentsBuilder.fromUriString(passwordResetUrl)
                .queryParam("token", rawToken)
                .build()
                .toUriString();

        SimpleMailMessage message = new SimpleMailMessage();
        if (StringUtils.hasText(mailFromAddress)) {
            message.setFrom(mailFromAddress);
        }
        message.setTo(user.getUniversityEmail());
        message.setSubject("Connect password reset");
        message.setText(buildPasswordResetEmailBody(user, resetLink, expiry));
        mailSender.send(message);
    }

    private String buildPasswordResetEmailBody(User user, String resetLink, LocalDateTime expiry) {
        String displayName = StringUtils.hasText(user.getFirstName()) ? user.getFirstName() : "there";
        return """
                Hello %s,

                We received a request to reset your Connect account password.

                Use this link to choose a new password:
                %s

                This link expires at %s and can only be used once.

                If you did not request this reset, you can ignore this email.
                """.formatted(displayName, resetLink, expiry);
    }

    private String generateSecureToken() {
        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available.", exception);
        }
    }

    private void clearPasswordResetToken(User user) {
        user.setPasswordResetTokenHash(null);
        user.setPasswordResetExpiresAt(null);
    }
}
