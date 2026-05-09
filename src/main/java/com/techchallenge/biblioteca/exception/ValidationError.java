package com.techchallenge.biblioteca.exception;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class ValidationError extends StandardError {

    private final List<FieldErrorDetail> errors = new ArrayList<>();

    public ValidationError(LocalDateTime timestamp, Integer status, String error, String message, String path) {
        super(timestamp, status, error, message, path);
    }

    public void addError(String field, String message) {
        errors.add(new FieldErrorDetail(field, message));
    }

    public record FieldErrorDetail(String field, String message) {
    }
}
