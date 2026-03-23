package auca.ac.rw.connect.repository;

import auca.ac.rw.connect.models.Project;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
}
