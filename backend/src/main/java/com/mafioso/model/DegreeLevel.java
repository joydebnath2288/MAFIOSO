package com.mafioso.model;

import java.util.Locale;

/**
 * Supported study degree levels for UK applications.
 */
public enum DegreeLevel {
    BSc("BSc"),
    MSc("MSc");

    private final String dbValue;

    DegreeLevel(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static DegreeLevel fromString(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("Degree level must not be empty");
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        if ("BSC".equals(normalized)) {
            return BSc;
        }
        if ("MSC".equals(normalized)) {
            return MSc;
        }
        throw new IllegalArgumentException("Degree level must be BSc or MSc");
    }
}
