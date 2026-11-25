package com.mafioso.bootstrap;

import com.mafioso.dto.UserResponse;
import com.mafioso.util.DatabaseMigrationRunner;

import java.io.Console;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * Operator-only CLI utility inside the backend container to provision {@code CONSULTANT} accounts
 * securely using BCrypt hashing without exposing any public registration endpoint.
 */
public final class ConsultantProvisionCli {

    private ConsultantProvisionCli() {
    }

    public static void main(String[] args) {
        try {
            DatabaseMigrationRunner.migrate();
        } catch (SQLException ex) {
            System.err.println("[MAFIOSO Staff CLI] Database migration check failed: " + ex.getMessage());
            System.exit(1);
            return;
        }

        String fullName = System.getenv("MAFIOSO_CONSULTANT_NAME");
        String email = System.getenv("MAFIOSO_CONSULTANT_EMAIL");
        String password = AdminBootstrapService.resolvePasswordFromEnvOrFile(
                "MAFIOSO_CONSULTANT_PASSWORD",
                "MAFIOSO_CONSULTANT_PASSWORD_FILE"
        );

        Console console = System.console();
        if ((fullName == null || fullName.trim().isEmpty()) && console != null) {
            fullName = console.readLine("Consultant Full Name: ");
        }
        if ((email == null || email.trim().isEmpty()) && console != null) {
            email = console.readLine("Consultant Email: ");
        }
        if ((password == null || password.isEmpty()) && console != null) {
            char[] passwordChars = console.readPassword("Consultant Password (hidden): ");
            char[] confirmChars = console.readPassword("Confirm Password (hidden): ");
            if (passwordChars == null || confirmChars == null || !Arrays.equals(passwordChars, confirmChars)) {
                clearChars(passwordChars);
                clearChars(confirmChars);
                System.err.println("[MAFIOSO Staff CLI] Passwords do not match.");
                System.exit(1);
                return;
            }
            clearChars(confirmChars);
            password = new String(passwordChars);
            clearChars(passwordChars);
        }

        if (isBlank(fullName) || isBlank(email) || isBlank(password)) {
            System.err.println("[MAFIOSO Staff CLI] Missing consultant details. Supply via interactive TTY "
                    + "or MAFIOSO_CONSULTANT_NAME, MAFIOSO_CONSULTANT_EMAIL, and MAFIOSO_CONSULTANT_PASSWORD.");
            System.exit(1);
            return;
        }

        try {
            AdminBootstrapService service = new AdminBootstrapService();
            UserResponse created = service.provisionConsultant(fullName, email, password);
            System.out.println("[MAFIOSO Staff CLI] CONSULTANT account created: id=" + created.getId()
                    + ", email=" + created.getEmail() + ", role=" + created.getRole());
        } catch (IllegalStateException ex) {
            System.err.println("[MAFIOSO Staff CLI] Refused: " + ex.getMessage());
            System.exit(2);
        } catch (IllegalArgumentException ex) {
            System.err.println("[MAFIOSO Staff CLI] Validation error: " + ex.getMessage());
            System.exit(1);
        } catch (SQLException ex) {
            System.err.println("[MAFIOSO Staff CLI] Database error: " + ex.getMessage());
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
