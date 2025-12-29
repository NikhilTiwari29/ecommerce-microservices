package com.nikhil.microservices.order.advices;

import java.util.List;

public record ApiErrorResponse(
        String message,
        List<FieldError> fieldErrors
) {
    public record FieldError(String field, String message) {}
}
