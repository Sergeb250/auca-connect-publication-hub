package auca.ac.rw.connect.repository;

import auca.ac.rw.connect.models.MemoirFile;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemoirFileRepository extends JpaRepository<MemoirFile, UUID> {
}
