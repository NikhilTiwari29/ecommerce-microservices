package com.nikhil.microservices.inventory.dto;

public record ApiResponse<T>(
        String timestamp,
        int status,
        String path,
        T data,
        ApiErrorResponse error
) {}
