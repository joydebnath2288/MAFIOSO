package com.mafioso.model;

import java.util.Locale;

/**
 * Supported document categories for student and application uploads.
 */
public enum DocumentCategory {
    PASSPORT,
    ACADEMIC_CERTIFICATE,
    ACADEMIC_TRANSCRIPT,
    ENGLISH_CERTIFICATE,
    CV,
    FINANCIAL_DOCUMENT,
    OTHER;

    public static DocumentCategory fromString(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("Document category must not be empty");
        }
        return DocumentCategory.valueOf(raw.trim().toUpperCase(Locale.ROOT));
    }
}
