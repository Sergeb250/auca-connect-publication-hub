package auca.ac.rw.connect.repository;

import auca.ac.rw.connect.models.Publication;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicationRepository extends JpaRepository<Publication, UUID> {
}
