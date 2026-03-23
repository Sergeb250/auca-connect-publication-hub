package auca.ac.rw.connect.repository;

import auca.ac.rw.connect.models.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUniversityEmail(String universityEmail);

    boolean existsByUniversityEmail(String universityEmail);

    Optional<User> findByPasswordResetTokenHash(String passwordResetTokenHash);
}
