package com.mafioso.model;

import java.util.Locale;

/**
 * Review lifecycle statuses for uploaded application documents.
 */
public enum DocumentReviewStatus {
    PENDING,
    UNDER_REVIEW,
    APPROVED,
    REJECTED;

    public static DocumentReviewStatus fromString(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("Document review status must not be empty");
        }
        return DocumentReviewStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
    }
}
