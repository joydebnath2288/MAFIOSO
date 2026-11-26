package com.mafioso.model;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

/**
 * Lifecycle statuses for student study applications.
 */
public enum ApplicationStatus {
    SUBMITTED,
    UNDER_REVIEW,
    ASSIGNED,
    DOCUMENTS_PENDING,
    DOCUMENTS_REVIEW,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;

    private static final Set<ApplicationStatus> CONSULTANT_ALLOWED_TARGETS = Collections.unmodifiableSet(
            EnumSet.of(UNDER_REVIEW, DOCUMENTS_PENDING, DOCUMENTS_REVIEW, IN_PROGRESS, COMPLETED)
    );

    public static ApplicationStatus fromString(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("Application status must not be empty");
        }
        return ApplicationStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
    }

    public boolean isConsultantAllowedTarget() {
        return CONSULTANT_ALLOWED_TARGETS.contains(this);
    }
}
