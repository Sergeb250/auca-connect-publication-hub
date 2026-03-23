package auca.ac.rw.connect.exception;

import auca.ac.rw.connect.dto.response.ErrorResponse;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AucaException.class)
    public ResponseEntity<ErrorResponse> handleAucaException(AucaException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        return ResponseEntity.status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode.getCode(), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));

        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(ErrorCode.ERR_VALIDATION.getCode(), message));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException exception) {
        return ResponseEntity.status(ErrorCode.ERR_AUTH_FORBIDDEN.getStatus())
                .body(ErrorResponse.of(ErrorCode.ERR_AUTH_FORBIDDEN.getCode(),
                        "You do not have permission for this action."));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException exception) {
        return ResponseEntity.status(ErrorCode.ERR_AUTH_401.getStatus())
                .body(ErrorResponse.of(ErrorCode.ERR_AUTH_401.getCode(), "Authentication is required."));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException exception) {
        return ResponseEntity.status(ErrorCode.ERR_NOT_FOUND.getStatus())
                .body(ErrorResponse.of(ErrorCode.ERR_NOT_FOUND.getCode(), "Requested resource was not found."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception exception) {
        log.error("Unexpected server error", exception);
        return ResponseEntity.status(ErrorCode.ERR_SERVER.getStatus())
                .body(ErrorResponse.of(ErrorCode.ERR_SERVER.getCode(),
                        "An unexpected internal server error occurred."));
    }

    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }
}
