package dev.challenge.common.error;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Object> handleCustom(CustomException ex) {
        return ResponseEntity.status(ex.getStatus()).body(ex.getMessage());
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
