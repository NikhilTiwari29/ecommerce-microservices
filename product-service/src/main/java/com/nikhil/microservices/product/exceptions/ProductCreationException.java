package com.nikhil.microservices.product.exceptions;

public class ProductCreationException extends RuntimeException {
    public ProductCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
