package auca.ac.rw.connect.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Database-backed platform configuration entry.
 */
@Entity
@Table(name = "system_settings", indexes = {
        @Index(name = "idx_system_settings_setting_key", columnList = "setting_key"),
        @Index(name = "idx_system_settings_category", columnList = "category")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SystemSetting extends BaseEntity {

    @Column(name = "setting_key", nullable = false, unique = true)
    private String settingKey;

    @Column(name = "setting_value", nullable = false, columnDefinition = "TEXT")
    private String settingValue;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private SettingCategory category;

    @Column(name = "last_modified_by")
    private String lastModifiedBy;

    public enum SettingCategory {
        RESERVATION,
        ACCESS_SCHEDULE,
        NOTIFICATIONS,
        BRANDING,
        FILE_UPLOAD,
        SECURITY
    }
}
