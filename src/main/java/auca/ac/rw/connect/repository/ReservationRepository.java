package auca.ac.rw.connect.repository;

import auca.ac.rw.connect.models.Reservation;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
}
