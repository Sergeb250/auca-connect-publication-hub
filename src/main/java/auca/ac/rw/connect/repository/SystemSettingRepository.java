package auca.ac.rw.connect.repository;

import auca.ac.rw.connect.models.SystemSetting;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemSettingRepository extends JpaRepository<SystemSetting, UUID> {

    Optional<SystemSetting> findBySettingKey(String settingKey);
}
