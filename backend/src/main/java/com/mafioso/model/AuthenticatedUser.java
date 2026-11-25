package com.mafioso.model;

import java.io.Serializable;

/**
 * Immutable authenticated user principal stored inside the server-side {@code HttpSession}.
 */
public final class AuthenticatedUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long id;
    private final String fullName;
    private final String email;
    private final Role role;

    public AuthenticatedUser(long id, String fullName, String email, Role role) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    public static AuthenticatedUser fromUser(User user) {
        return new AuthenticatedUser(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole()
        );
    }

    public long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    @Override
    public String toString() {
        return "AuthenticatedUser{id=" + id + ", role=" + role + '}';
    }
}
