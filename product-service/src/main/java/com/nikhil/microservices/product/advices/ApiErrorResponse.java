package com.nikhil.microservices.product.advices;

import java.util.List;

public record ApiErrorResponse(
        String message,
        List<FieldError> fieldErrors
) {
    public record FieldError(String field, String message) {}
}
