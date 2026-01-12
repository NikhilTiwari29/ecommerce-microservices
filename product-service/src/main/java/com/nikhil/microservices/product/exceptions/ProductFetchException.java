package com.nikhil.microservices.product.exceptions;

public class ProductFetchException extends RuntimeException{
    public ProductFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}
