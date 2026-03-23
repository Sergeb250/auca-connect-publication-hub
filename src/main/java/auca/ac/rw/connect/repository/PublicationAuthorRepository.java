package auca.ac.rw.connect.repository;

import auca.ac.rw.connect.models.PublicationAuthor;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicationAuthorRepository extends JpaRepository<PublicationAuthor, UUID> {
}
