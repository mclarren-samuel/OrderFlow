package com.orderflow.orders.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.stream.Collectors;

public class GlobalExceptionHandler {
    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> errors =
                exception.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .collect(Collectors.toMap(
                                error -> error.getField(),
                                error -> error.getDefaultMessage() == null
                                        ? "Invalid value"
                                        : error.getDefaultMessage(),
                                (first, second) -> first
                        ));

        return ResponseEntity.badRequest().body(
                Map.of(
                        "status", 400,
                        "error", "Validation failed",
                        "details", errors
                )
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ProblemDetail> handleStatusException(
            ResponseStatusException exception
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                exception.getStatusCode(),
                exception.getReason() == null
                        ? "Request failed"
                        : exception.getReason()
        );

        problem.setTitle("Request failed");

        return ResponseEntity
                .status(exception.getStatusCode())
                .body(problem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleMalformedJson(
            HttpMessageNotReadableException exception
    ) {
        return ResponseEntity.badRequest().body(
                Map.of(
                        "status", 400,
                        "error", "Malformed or invalid JSON request"
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(
            Exception exception
    ) {
        log.error("Unexpected application error", exception);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        Map.of(
                                "status", 500,
                                "error", "An unexpected error occurred"
                        )
                );
    }
}
