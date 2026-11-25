package com.mafioso.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Payload returned upon authentication actions (login, registration, logout).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private UserResponse user;
    private String csrfToken;
    private String redirectTo;

    public AuthResponse() {
    }

    public AuthResponse(UserResponse user, String csrfToken, String redirectTo) {
        this.user = user;
        this.csrfToken = csrfToken;
        this.redirectTo = redirectTo;
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }

    public String getCsrfToken() {
        return csrfToken;
    }

    public void setCsrfToken(String csrfToken) {
        this.csrfToken = csrfToken;
    }

    public String getRedirectTo() {
        return redirectTo;
    }

    public void setRedirectTo(String redirectTo) {
        this.redirectTo = redirectTo;
    }
}
