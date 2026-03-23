package auca.ac.rw.connect.repository;

import auca.ac.rw.connect.models.ModerationAction;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModerationActionRepository extends JpaRepository<ModerationAction, UUID> {
}
