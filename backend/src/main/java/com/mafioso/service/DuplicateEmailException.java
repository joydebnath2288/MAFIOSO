package com.mafioso.service;

/**
 * Thrown when attempting to register an email address that already exists.
 */
public class DuplicateEmailException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DuplicateEmailException(String message) {
        super(message);
    }
}
