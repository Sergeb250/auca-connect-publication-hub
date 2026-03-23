package auca.ac.rw.connect.security;

import auca.ac.rw.connect.models.User;
import auca.ac.rw.connect.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByUniversityEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return buildUserDetails(user);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(String userId) {
        UUID parsedUserId;
        try {
            parsedUserId = UUID.fromString(userId);
        } catch (IllegalArgumentException exception) {
            throw new UsernameNotFoundException("Invalid user ID in token.");
        }

        User user = userRepository.findById(parsedUserId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));
        return buildUserDetails(user);
    }

    private AucaUserDetails buildUserDetails(User user) {
        return AucaUserDetails.builder()
                .userId(user.getId())
                .email(user.getUniversityEmail())
                .passwordHash(user.getPasswordHash())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                .accountNonLocked(user.getStatus() != User.UserStatus.SUSPENDED)
                .enabled(user.getStatus() == User.UserStatus.ACTIVE)
                .build();
    }
}
