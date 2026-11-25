package com.mafioso.service;

/**
 * Thrown when an authenticated account is disabled or suspended.
 */
public class AccountDisabledException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AccountDisabledException(String message) {
        super(message);
    }
}
