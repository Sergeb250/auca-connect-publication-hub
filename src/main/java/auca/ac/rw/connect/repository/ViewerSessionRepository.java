package auca.ac.rw.connect.repository;

import auca.ac.rw.connect.models.ViewerSession;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ViewerSessionRepository extends JpaRepository<ViewerSession, UUID> {
}
