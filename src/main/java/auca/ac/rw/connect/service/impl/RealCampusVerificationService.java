package auca.ac.rw.connect.service.impl;

import auca.ac.rw.connect.service.CampusVerificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * Placeholder for the future AUCA campus verification API integration.
 *
 * <p>This class is not yet active.
 *
 * <p>To activate it when the real API is ready:
 * add {@code @Primary} to this class,
 * remove {@code @Primary} from {@link SimpleCampusVerificationService},
 * and configure {@code campus.db.api-url} and {@code campus.db.api-key} in application properties.
 *
 * <p>The expected API contract should be documented here when AUCA ICT provides the specification.
 */
@Slf4j
@Service
public class RealCampusVerificationService implements CampusVerificationService {

    @Override
    @PreAuthorize("permitAll()")
    public CampusData verify(String email, String campusId) {
        throw new UnsupportedOperationException(
                "The real AUCA campus verification API is not yet integrated.");
    }
}
