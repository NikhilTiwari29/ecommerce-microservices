package com.nikhil.microservices.product.advices;

import com.nikhil.microservices.product.exceptions.ProductCreationException;
import com.nikhil.microservices.product.exceptions.ProductNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss z")
                    .withZone(ZoneId.of("Asia/Kolkata"));

    /* -------------------------------------------------------
       Common error response builder
       ------------------------------------------------------- */
    private ApiResponse<Void> errorResponse(
            HttpStatus status,
            String message,
            String path,
            ApiErrorResponse error) {

        return new ApiResponse<>(
                FORMATTER.format(Instant.now()),
                status.value(),
                path,
                null,
                error
        );
    }

    /* -------------------------------------------------------
       Validation Errors (400)
       ------------------------------------------------------- */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<ApiErrorResponse.FieldError> fieldErrors =
                ex.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(err -> new ApiErrorResponse.FieldError(
                                err.getField(),
                                err.getDefaultMessage()
                        ))
                        .toList();

        log.warn(
                "Validation failed | path={} | errors={}",
                request.getRequestURI(),
                fieldErrors
        );

        ApiErrorResponse error =
                new ApiErrorResponse("Validation failed", fieldErrors);

        return ResponseEntity.badRequest()
                .body(errorResponse(
                        HttpStatus.BAD_REQUEST,
                        "Validation failed",
                        request.getRequestURI(),
                        error
                ));
    }

    /* -------------------------------------------------------
       Malformed JSON (400)
       ------------------------------------------------------- */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMalformedJson(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        log.warn(
                "Malformed JSON request | path={}",
                request.getRequestURI()
        );

        ApiErrorResponse error =
                new ApiErrorResponse("Malformed JSON request", null);

        return ResponseEntity.badRequest()
                .body(errorResponse(
                        HttpStatus.BAD_REQUEST,
                        "Malformed JSON request",
                        request.getRequestURI(),
                        error
                ));
    }

    /* -------------------------------------------------------
       Business Exceptions
       ------------------------------------------------------- */

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleProductNotFound(
            ProductNotFoundException ex,
            HttpServletRequest request) {

        log.warn(
                "Product not found | path={} | message={}",
                request.getRequestURI(),
                ex.getMessage()
        );

        ApiErrorResponse error =
                new ApiErrorResponse(ex.getMessage(), null);

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(errorResponse(
                        HttpStatus.NOT_FOUND,
                        ex.getMessage(),
                        request.getRequestURI(),
                        error
                ));
    }

    @ExceptionHandler(ProductCreationException.class)
    public ResponseEntity<ApiResponse<Void>> handleProductCreationException(
            ProductCreationException ex,
            HttpServletRequest request) {

        log.error(
                "Product creation failed | path={}",
                request.getRequestURI(),
                ex
        );

        ApiErrorResponse error =
                new ApiErrorResponse(ex.getMessage(), null);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        ex.getMessage(),
                        request.getRequestURI(),
                        error
                ));
    }

    /* -------------------------------------------------------
       Fallback – Unexpected System Errors (500)
       ------------------------------------------------------- */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            RuntimeException ex,
            HttpServletRequest request) {

        // Do not interfere with actuator endpoints
        if (request.getRequestURI().startsWith("/actuator")) {
            throw ex;
        }

        // 🔴 Full stack trace for logs
        log.error(
                "Unhandled exception | path={} | method={}",
                request.getRequestURI(),
                request.getMethod(),
                ex
        );

        // 🟢 Safe response for client
        ApiErrorResponse error =
                new ApiErrorResponse("Internal server error", null);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Something went wrong. Please try again later.",
                        request.getRequestURI(),
                        error
                ));
    }
}
