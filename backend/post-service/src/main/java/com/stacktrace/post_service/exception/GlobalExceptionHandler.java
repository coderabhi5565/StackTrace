package com.stacktrace.post_service.exception;

import com.stacktrace.post_service.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({PostNotFoundException.class, TagNotFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ErrorResponse> notFound(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req, null);
    }

    @ExceptionHandler({PostAlreadyPublishedException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ErrorResponse> conflict(Exception ex, HttpServletRequest req) {
        String msg = ex instanceof DataIntegrityViolationException ? "Conflicting update, please retry" : ex.getMessage();
        return build(HttpStatus.CONFLICT, msg, req, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> forbidden(AccessDeniedException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), req, null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> unauthorized(AuthenticationException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), req, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> errors.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return build(HttpStatus.BAD_REQUEST, "Validation failed", req, errors);
    }

    @ExceptionHandler({ImageUploadFailedException.class, PostNotScheduledException.class,
            IllegalArgumentException.class, IllegalStateException.class, ConstraintViolationException.class,
            HandlerMethodValidationException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class, HttpMessageNotReadableException.class,
            MaxUploadSizeExceededException.class})
    public ResponseEntity<ErrorResponse> badRequest(Exception ex, HttpServletRequest req) {
        String msg = switch (ex) {
            case HttpMessageNotReadableException e -> "Malformed request body";
            case MaxUploadSizeExceededException e -> "File too large";
            case HandlerMethodValidationException e -> "Invalid request parameters";
            default -> ex.getMessage();
        };
        return build(HttpStatus.BAD_REQUEST, msg, req, null);
    }

    @ExceptionHandler(UploadsNotConfiguredException.class)
    public ResponseEntity<ErrorResponse> unavailable(UploadsNotConfiguredException ex, HttpServletRequest req) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), req, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> fallback(Exception ex, HttpServletRequest req) {
        log.error("Unhandled error on {} {}", req.getMethod(), req.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong", req, null);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest req,
                                                Map<String, String> validationErrors) {
        return ResponseEntity.status(status).body(ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(req.getRequestURI())
                .validationErrors(validationErrors)
                .build());
    }
}
