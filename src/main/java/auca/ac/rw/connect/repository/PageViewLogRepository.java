package auca.ac.rw.connect.repository;

import auca.ac.rw.connect.models.PageViewLog;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PageViewLogRepository extends JpaRepository<PageViewLog, UUID> {
}
