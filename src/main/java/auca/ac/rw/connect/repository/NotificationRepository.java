package auca.ac.rw.connect.repository;

import auca.ac.rw.connect.models.Notification;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
}
