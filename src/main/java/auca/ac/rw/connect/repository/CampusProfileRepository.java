package auca.ac.rw.connect.repository;

import auca.ac.rw.connect.models.CampusProfile;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampusProfileRepository extends JpaRepository<CampusProfile, UUID> {

    Optional<CampusProfile> findByUserId(UUID userId);

    Optional<CampusProfile> findByCampusId(String campusId);

    boolean existsByCampusId(String campusId);
}
