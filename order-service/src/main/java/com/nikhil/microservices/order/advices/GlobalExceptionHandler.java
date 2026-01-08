package com.nikhil.microservices.order.advices;

import com.nikhil.microservices.order.exceptions.InsufficientInventoryException;
import com.nikhil.microservices.order.exceptions.InventoryUnavailableException;
import com.nikhil.microservices.order.exceptions.OrderCreationException;
import jakarta.servlet.http.HttpServletRequest;
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

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss z")
                    .withZone(ZoneId.of("Asia/Kolkata"));

    private ApiResponse<Void> errorResponse(HttpStatus status, String message, String path, ApiErrorResponse error) {
        return new ApiResponse<>(
                FORMATTER.format(Instant.now()),
                status.value(),
                path,
                null,
                error
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<ApiErrorResponse.FieldError> fieldErrors =
                ex.getBindingResult().getFieldErrors().stream()
                        .map(err -> new ApiErrorResponse.FieldError(err.getField(), err.getDefaultMessage()))
                        .toList();

        ApiErrorResponse error = new ApiErrorResponse("Validation failed", fieldErrors);

        return ResponseEntity.badRequest()
                .body(errorResponse(HttpStatus.BAD_REQUEST, "Validation failed", request.getRequestURI(), error));
    }

    @ExceptionHandler(OrderCreationException.class)
    public ResponseEntity<ApiResponse<Void>> handleOrderCreationException(
            OrderCreationException ex,
            HttpServletRequest request) {

        ApiErrorResponse error = new ApiErrorResponse(ex.getMessage(), null);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request.getRequestURI(), error));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMalformedJson(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        ApiErrorResponse error = new ApiErrorResponse("Malformed JSON request", null);

        return ResponseEntity.badRequest()
                .body(errorResponse(HttpStatus.BAD_REQUEST, "Malformed JSON request", request.getRequestURI(), error));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            RuntimeException ex,
            HttpServletRequest request) {

        // 🔹 Do NOT intercept actuator errors
        if (request.getRequestURI().startsWith("/actuator")) {
            throw ex;
        }

        ApiErrorResponse error = new ApiErrorResponse("Internal server error", null);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", request.getRequestURI(), error));
    }

    @ExceptionHandler(InventoryUnavailableException.class)
    public ResponseEntity<ApiResponse<Void>> handleInventoryUnavailable(
            InventoryUnavailableException ex,
            HttpServletRequest request) {

        ApiErrorResponse error = new ApiErrorResponse(ex.getMessage(), null);

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(errorResponse(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), request.getRequestURI(), error));
    }

    @ExceptionHandler(InsufficientInventoryException.class)
    public ResponseEntity<ApiResponse<Void>> handleInsufficientInventory(
            InsufficientInventoryException ex,
            HttpServletRequest request) {

        ApiErrorResponse error = new ApiErrorResponse(ex.getMessage(), null);

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(errorResponse(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI(), error));
    }


}
