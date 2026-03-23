package auca.ac.rw.connect.service.impl;

import auca.ac.rw.connect.exception.AucaException;
import auca.ac.rw.connect.exception.ErrorCode;
import auca.ac.rw.connect.repository.CampusProfileRepository;
import auca.ac.rw.connect.service.CampusVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Primary
@Service
@RequiredArgsConstructor
public class SimpleCampusVerificationService implements CampusVerificationService {

    private static final int MIN_CAMPUS_ID = 20000;
    private static final int MAX_CAMPUS_ID = 29000;

    private final CampusProfileRepository campusProfileRepository;

    @Override
    @PreAuthorize("permitAll()")
    @Transactional(readOnly = true)
    public CampusData verify(String email, String campusId) {
        log.info("Campus verification running in simple mode for email {}. No external AUCA campus API is connected.",
                email);

        if (campusId == null || campusId.isBlank()) {
            throw new AucaException(ErrorCode.ERR_VER_404, "Campus ID is required.");
        }

        int numericCampusId;
        try {
            numericCampusId = Integer.parseInt(campusId.trim());
        } catch (NumberFormatException exception) {
            throw new AucaException(ErrorCode.ERR_VER_404,
                    "Invalid Campus ID  Try Agian Or contact the administration Office.");
        }

        if (numericCampusId < MIN_CAMPUS_ID || numericCampusId > MAX_CAMPUS_ID) {
            throw new AucaException(ErrorCode.ERR_VER_404,
                    "Campus ID is not  valid .");
        }

        if (campusProfileRepository.existsByCampusId(campusId.trim())) {
            throw new AucaException(ErrorCode.ERR_AUTH_409,
                    "This campus ID is already registered to another account.");
        }

        return CampusData.builder()
                .campusId(campusId.trim())
                .academicLevel("UNDERGRADUATE")
                .enrolled(true)
                .build();
    }
}
