package com.techchallenge.biblioteca.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<StandardError> handleResourceNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        log.warn("Resource not found. path={}, message={}", request.getRequestURI(), exception.getMessage());

        StandardError error = buildStandardError(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<StandardError> handleBusinessException(
            BusinessException exception,
            HttpServletRequest request
    ) {
        log.warn("Business rule violation. path={}, message={}", request.getRequestURI(), exception.getMessage());

        StandardError error = buildStandardError(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationError> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        log.warn("Validation failed. path={}, errorCount={}", request.getRequestURI(), exception.getErrorCount());

        ValidationError error = new ValidationError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Validation failed.",
                request.getRequestURI()
        );

        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            error.addError(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<StandardError> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        String message = "Invalid value for request parameter '" + exception.getName() + "'.";
        log.warn("Request parameter type mismatch. path={}, parameter={}", request.getRequestURI(), exception.getName());

        StandardError error = buildStandardError(
                HttpStatus.BAD_REQUEST,
                message,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<StandardError> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        log.warn("Malformed request body. path={}", request.getRequestURI());

        StandardError error = buildStandardError(
                HttpStatus.BAD_REQUEST,
                "Request body is invalid or malformed.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<StandardError> handleDataIntegrityViolation(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        log.warn("Data integrity violation. path={}, message={}", request.getRequestURI(), exception.getMessage());

        StandardError error = buildStandardError(
                HttpStatus.CONFLICT,
                "The operation violates a data integrity constraint.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<StandardError> handleOptimisticLockingFailure(
            OptimisticLockingFailureException exception,
            HttpServletRequest request
    ) {
        log.warn("Optimistic locking conflict. path={}, message={}", request.getRequestURI(), exception.getMessage());

        StandardError error = buildStandardError(
                HttpStatus.CONFLICT,
                "The resource was modified concurrently. Please reload and try again.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardError> handleException(
            Exception exception,
            HttpServletRequest request
    ) {
        log.error("Unexpected internal error. path={}", request.getRequestURI(), exception);

        StandardError error = buildStandardError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected internal error occurred.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    private StandardError buildStandardError(HttpStatus status, String message, String path) {
        return new StandardError(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path
        );
    }
}
