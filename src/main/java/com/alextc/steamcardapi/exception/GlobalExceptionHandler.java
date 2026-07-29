package com.alextc.steamcardapi.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({InvalidThemeException.class, InvalidCardParameterException.class})
    ResponseEntity<ApiError> handleInvalidRequest(RuntimeException exception, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Invalid request", exception.getMessage(), request);
    }

    @ExceptionHandler(SteamProfileNotFoundException.class)
    ResponseEntity<ApiError> handleProfileNotFound(
            SteamProfileNotFoundException exception,
            HttpServletRequest request
    ) {
        return build(HttpStatus.NOT_FOUND, "Steam profile not found", "Steam profile could not be found", request);
    }

    @ExceptionHandler(SteamGameNotFoundException.class)
    ResponseEntity<ApiError> handleGameNotFound(SteamGameNotFoundException exception, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "Steam game not found", "Steam game could not be found", request);
    }

    @ExceptionHandler(SteamApiException.class)
    ResponseEntity<ApiError> handleSteamApi(SteamApiException exception, HttpServletRequest request) {
        return build(HttpStatus.BAD_GATEWAY, "Steam API error", "Unable to retrieve Steam profile", request);
    }

    @ExceptionHandler({
            ConstraintViolationException.class,
            MethodArgumentNotValidException.class,
            MethodArgumentTypeMismatchException.class
    })
    ResponseEntity<ApiError> handleValidation(Exception exception, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Invalid request", "Request validation failed", request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> handleUnexpected(Exception exception, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", "Unexpected server error", request);
    }

    private ResponseEntity<ApiError> build(
            HttpStatus status,
            String error,
            String message,
            HttpServletRequest request
    ) {
        ApiError body = new ApiError(Instant.now(), status.value(), error, message,
                request.getRequestURI(), UUID.randomUUID().toString());
        return ResponseEntity.status(status).body(body);
    }
}
