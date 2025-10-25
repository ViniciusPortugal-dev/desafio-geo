package dev.challenge.common.error;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<?> handleCustom(CustomException ex, HttpServletRequest req) {
        HttpStatus status = ex.getStatus();

        String message = ex.getMessage();
        if (message != null) {
            String trimmed = message.trim();
            if ((trimmed.startsWith("{") && trimmed.endsWith("}")) ||
                    (trimmed.startsWith("[") && trimmed.endsWith("]"))) {
                return ResponseEntity.status(status).body(trimmed);
            }
        }

        ApiError body = ApiError.of(
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                req.getRequestURI(),
                ex.getDetails()
        );
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ApiError body = ApiError.of(
                status.value(),
                status.getReasonPhrase(),
                "Erro inesperado: " + ex.getMessage(),
                req.getRequestURI(),
                null
        );
        return ResponseEntity.status(status).body(body);
    }
}
