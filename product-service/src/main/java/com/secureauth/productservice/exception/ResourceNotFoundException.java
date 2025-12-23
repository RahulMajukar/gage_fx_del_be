package com.secureauth.productservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a requested resource is not found.
 * This exception will result in a 404 Not Found HTTP response.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructs a new ResourceNotFoundException with the specified detail message.
     *
     * @param message the detail message
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new ResourceNotFoundException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new ResourceNotFoundException with a formatted message for entity not found.
     *
     * @param entityName the name of the entity (e.g., "Manufacturer", "Supplier", "Gage")
     * @param fieldName  the name of the field used for lookup (e.g., "id", "name")
     * @param fieldValue the value of the field that was not found
     */
    public ResourceNotFoundException(String entityName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: %s", entityName, fieldName, fieldValue));
    }
} 