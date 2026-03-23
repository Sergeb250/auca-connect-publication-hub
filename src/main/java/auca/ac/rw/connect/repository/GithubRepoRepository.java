package auca.ac.rw.connect.repository;

import auca.ac.rw.connect.models.GithubRepo;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GithubRepoRepository extends JpaRepository<GithubRepo, UUID> {
}
