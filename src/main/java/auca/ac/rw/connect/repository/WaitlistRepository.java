package auca.ac.rw.connect.repository;

import auca.ac.rw.connect.models.Waitlist;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WaitlistRepository extends JpaRepository<Waitlist, UUID> {
}
