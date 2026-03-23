package auca.ac.rw.connect.util;

import auca.ac.rw.connect.exception.AucaException;
import auca.ac.rw.connect.exception.ErrorCode;
import auca.ac.rw.connect.security.AucaUserDetails;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AucaUserDetails userDetails)) {
            throw new AucaException(ErrorCode.ERR_AUTH_401, "Not authenticated.");
        }
        return userDetails.getUserId();
    }
}
