package auca.ac.rw.connect.security;

import auca.ac.rw.connect.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtTokenProvider {

    private static final String EMAIL_CLAIM = "email";
    private static final String ROLE_CLAIM = "role";
    private static final String PROJECT_ID_CLAIM = "projectId";
    private static final String TOKEN_TYPE_CLAIM = "type";
    private static final String VIEWER_SESSION_TYPE = "VIEWER_SESSION";

    private final SecretKey signingKey;
    private final long expirationHours;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-hours}") long expirationHours) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationHours = expirationHours;
    }

    public String generateLoginToken(User user) {
        Date now = new Date();
        Date expiry = Date.from(now.toInstant().plus(expirationHours, ChronoUnit.HOURS));

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim(EMAIL_CLAIM, user.getUniversityEmail())
                .claim(ROLE_CLAIM, user.getRole().name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    public String generateViewerToken(UUID userId, UUID projectId, LocalDateTime slotEnd) {
        Date now = new Date();
        Date expiry = Date.from(slotEnd.atZone(ZoneId.systemDefault()).toInstant());

        return Jwts.builder()
                .subject(userId.toString())
                .claim(PROJECT_ID_CLAIM, projectId.toString())
                .claim(TOKEN_TYPE_CLAIM, VIEWER_SESSION_TYPE)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    public boolean validateViewerToken(String token, UUID userId, UUID projectId) {
        try {
            Claims claims = extractClaims(token);
            String tokenUserId = claims.getSubject();
            String tokenProjectId = claims.get(PROJECT_ID_CLAIM, String.class);
            return !isTokenExpired(token)
                    && userId.toString().equals(tokenUserId)
                    && projectId.toString().equals(tokenProjectId);
        } catch (Exception exception) {
            return false;
        }
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(extractClaims(token).getSubject());
    }

    public String extractEmail(String token) {
        return extractClaims(token).get(EMAIL_CLAIM, String.class);
    }

    public String extractRole(String token) {
        return extractClaims(token).get(ROLE_CLAIM, String.class);
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractClaims(token).getExpiration().before(new Date());
        } catch (Exception exception) {
            return true;
        }
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = extractClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception exception) {
            return false;
        }
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
