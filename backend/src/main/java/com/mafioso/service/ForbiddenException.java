package com.mafioso.service;

/**
 * Thrown when an authenticated principal attempts to access or modify a resource outside their ownership or assignment scope.
 */
public class ForbiddenException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ForbiddenException(String message) {
        super(message);
    }
}
