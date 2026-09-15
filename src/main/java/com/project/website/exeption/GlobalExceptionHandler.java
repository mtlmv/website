package com.project.website.exeption;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, String> handleForbidden(
            ForbiddenException exception
    ){
        return Map.of(
                "error", "Forbidden",
                "message", exception.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidation(
            MethodArgumentNotValidException exception
    ){
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return Map.of(
                "error", "Validation failed",
                "message", message
        );
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleRuntime(
            RuntimeException exception
    ){
        return Map.of(
                "error", "Bad Request",
                "message", exception.getMessage() == null ? "Unexpected error" : exception.getMessage()
        );
    }
}
