package com.mafioso.bootstrap;

import com.mafioso.dto.UserResponse;
import com.mafioso.util.DatabaseMigrationRunner;

import java.io.Console;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * Interactive or environment-driven CLI tool for securely creating the initial {@code ADMIN} account.
 * Refuses to execute if an administrator already exists in the database.
 */
public final class AdminBootstrapCli {

    private AdminBootstrapCli() {
    }

    public static void main(String[] args) {
        try {
            DatabaseMigrationRunner.migrate();
        } catch (SQLException ex) {
            System.err.println("[MAFIOSO Bootstrap] Database migration check failed: " + ex.getMessage());
            System.exit(1);
            return;
        }

        String fullName = System.getenv("MAFIOSO_BOOTSTRAP_ADMIN_NAME");
        String email = System.getenv("MAFIOSO_BOOTSTRAP_ADMIN_EMAIL");
        String password = AdminBootstrapService.resolvePasswordFromEnvOrFile(
                "MAFIOSO_BOOTSTRAP_ADMIN_PASSWORD",
                "MAFIOSO_BOOTSTRAP_ADMIN_PASSWORD_FILE"
        );

        Console console = System.console();
        if ((fullName == null || fullName.trim().isEmpty()) && console != null) {
            fullName = console.readLine("Initial Admin Full Name: ");
        }
        if ((email == null || email.trim().isEmpty()) && console != null) {
            email = console.readLine("Initial Admin Email: ");
        }
        char[] passwordChars = null;
        if ((password == null || password.isEmpty()) && console != null) {
            passwordChars = console.readPassword("Initial Admin Password (hidden): ");
            char[] confirmChars = console.readPassword("Confirm Password (hidden): ");
            if (passwordChars == null || confirmChars == null || !Arrays.equals(passwordChars, confirmChars)) {
                clearChars(passwordChars);
                clearChars(confirmChars);
                System.err.println("[MAFIOSO Bootstrap] Passwords do not match.");
                System.exit(1);
                return;
            }
            clearChars(confirmChars);
            password = new String(passwordChars);
            clearChars(passwordChars);
        }

        if (isBlank(fullName) || isBlank(email) || isBlank(password)) {
            System.err.println("[MAFIOSO Bootstrap] Missing admin details. Supply via interactive TTY "
                    + "or MAFIOSO_BOOTSTRAP_ADMIN_NAME, MAFIOSO_BOOTSTRAP_ADMIN_EMAIL, and "
                    + "MAFIOSO_BOOTSTRAP_ADMIN_PASSWORD / MAFIOSO_BOOTSTRAP_ADMIN_PASSWORD_FILE.");
            System.exit(1);
            return;
        }

        try {
            AdminBootstrapService service = new AdminBootstrapService();
            UserResponse created = service.bootstrapInitialAdmin(fullName, email, password);
            System.out.println("[MAFIOSO Bootstrap] Initial ADMIN created: id=" + created.getId()
                    + ", email=" + created.getEmail() + ", role=" + created.getRole());
        } catch (IllegalStateException ex) {
            System.err.println("[MAFIOSO Bootstrap] Refused: " + ex.getMessage());
            System.exit(2);
        } catch (IllegalArgumentException ex) {
            System.err.println("[MAFIOSO Bootstrap] Validation error: " + ex.getMessage());
            System.exit(1);
        } catch (SQLException ex) {
            System.err.println("[MAFIOSO Bootstrap] Database error: " + ex.getMessage());
            System.exit(1);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static void clearChars(char[] chars) {
        if (chars != null) {
            Arrays.fill(chars, '\0');
        }
    }
}
