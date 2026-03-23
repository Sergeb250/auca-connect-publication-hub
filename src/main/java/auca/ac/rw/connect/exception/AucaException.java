package auca.ac.rw.connect.exception;

import lombok.Getter;

@Getter
public class AucaException extends RuntimeException {

    private final ErrorCode errorCode;

    public AucaException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
