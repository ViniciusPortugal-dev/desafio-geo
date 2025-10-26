package dev.challenge.common.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {
    private final HttpStatus status;
    private final Object details;

    public CustomException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.details = null;
    }

}
