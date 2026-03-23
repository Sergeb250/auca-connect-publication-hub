package auca.ac.rw.connect.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    ERR_VER_404("ERR-VER-404", HttpStatus.NOT_FOUND),
    ERR_VER_403("ERR-VER-403", HttpStatus.FORBIDDEN),
    ERR_VER_503("ERR-VER-503", HttpStatus.SERVICE_UNAVAILABLE),
    ERR_AUTH_401("ERR-AUTH-401", HttpStatus.UNAUTHORIZED),
    ERR_AUTH_202("ERR-AUTH-202", HttpStatus.ACCEPTED),
    ERR_AUTH_403("ERR-AUTH-403", HttpStatus.FORBIDDEN),
    ERR_AUTH_400("ERR-AUTH-400", HttpStatus.BAD_REQUEST),
    ERR_AUTH_409("ERR-AUTH-409", HttpStatus.CONFLICT),
    ERR_AUTH_429("ERR-AUTH-429", HttpStatus.TOO_MANY_REQUESTS),
    ERR_NOT_FOUND("ERR-NOT-FOUND", HttpStatus.NOT_FOUND),
    ERR_VALIDATION("ERR-VALIDATION", HttpStatus.BAD_REQUEST),
    ERR_AUTH_FORBIDDEN("ERR-AUTH-FORBIDDEN", HttpStatus.FORBIDDEN),
    ERR_CONFIG("ERR-CONFIG", HttpStatus.INTERNAL_SERVER_ERROR),
    ERR_SERVER("ERR-SERVER", HttpStatus.INTERNAL_SERVER_ERROR),
    ERR_RES_001("ERR-RES-001", HttpStatus.CONFLICT),
    ERR_RES_002("ERR-RES-002", HttpStatus.BAD_REQUEST),
    ERR_RES_003("ERR-RES-003", HttpStatus.BAD_REQUEST),
    ERR_RES_004("ERR-RES-004", HttpStatus.FORBIDDEN),
    ERR_RES_005("ERR-RES-005", HttpStatus.CONFLICT),
    ERR_RES_006("ERR-RES-006", HttpStatus.BAD_REQUEST),
    ERR_RES_007("ERR-RES-007", HttpStatus.CONFLICT),
    ERR_RES_008("ERR-RES-008", HttpStatus.GONE),
    ERR_RES_009("ERR-RES-009", HttpStatus.SERVICE_UNAVAILABLE),
    ERR_ACC_403("ERR-ACC-403", HttpStatus.FORBIDDEN),
    ERR_ACC_408("ERR-ACC-408", HttpStatus.REQUEST_TIMEOUT),
    ERR_ACC_409("ERR-ACC-409", HttpStatus.CONFLICT),
    ERR_UPL_001("ERR-UPL-001", HttpStatus.BAD_REQUEST),
    ERR_UPL_002("ERR-UPL-002", HttpStatus.PAYLOAD_TOO_LARGE),
    ERR_UPL_003("ERR-UPL-003", HttpStatus.UNPROCESSABLE_ENTITY),
    ERR_UPL_004("ERR-UPL-004", HttpStatus.CONFLICT),
    ERR_GIT_001("ERR-GIT-001", HttpStatus.NOT_FOUND),
    ERR_GIT_002("ERR-GIT-002", HttpStatus.NOT_FOUND),
    ERR_GIT_003("ERR-GIT-003", HttpStatus.BAD_REQUEST),
    ERR_GIT_004("ERR-GIT-004", HttpStatus.TOO_MANY_REQUESTS);

    private final String code;
    private final HttpStatus status;
}
