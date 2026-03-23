package auca.ac.rw.connect.repository;

import auca.ac.rw.connect.models.PublicationFile;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicationFileRepository extends JpaRepository<PublicationFile, UUID> {
}
