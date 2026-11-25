package com.mafioso.service;

/**
 * Thrown when a requested target resource does not exist for the expected entity type or role.
 */
public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
