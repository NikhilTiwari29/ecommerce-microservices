package com.nikhil.microservices.inventory.advices;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        String timestamp,
        int status,
        String path,
        T data,
        ApiErrorResponse error
) {
}
