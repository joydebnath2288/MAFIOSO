package com.mafioso.service;

/**
 * Thrown when authentication credentials are invalid or missing.
 */
public class AuthenticationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AuthenticationException(String message) {
        super(message);
    }
}
