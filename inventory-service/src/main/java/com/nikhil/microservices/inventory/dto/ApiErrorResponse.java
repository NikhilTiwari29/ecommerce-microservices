package com.nikhil.microservices.inventory.dto;

import java.util.List;

public record ApiErrorResponse(
        String message,
        List<FieldError> fieldErrors
) {
    public record FieldError(String field, String message) {}
}
