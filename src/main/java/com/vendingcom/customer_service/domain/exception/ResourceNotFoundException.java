package com.vendingcom.customer_service.domain.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public String getCode() {
        return "RESOURCE_NOT_FOUND";
    }
}
