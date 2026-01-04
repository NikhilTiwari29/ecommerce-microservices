package com.nikhil.microservices.order.exceptions;

public class InventoryUnavailableException extends RuntimeException {
    public InventoryUnavailableException(String message) {
        super(message);
    }

    public InventoryUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}