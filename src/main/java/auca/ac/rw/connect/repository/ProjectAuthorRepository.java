package auca.ac.rw.connect.repository;

import auca.ac.rw.connect.models.ProjectAuthor;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectAuthorRepository extends JpaRepository<ProjectAuthor, UUID> {
}
