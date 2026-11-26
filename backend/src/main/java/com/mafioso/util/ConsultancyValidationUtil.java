package com.mafioso.util;

import com.mafioso.dto.AssignConsultantRequest;
import com.mafioso.dto.CreateApplicationRequest;
import com.mafioso.dto.ReviewDocumentRequest;
import com.mafioso.dto.StudentProfileRequest;
import com.mafioso.dto.UpdateApplicationStatusRequest;
import com.mafioso.model.ApplicationStatus;
import com.mafioso.model.DegreeLevel;
import com.mafioso.model.DocumentReviewStatus;
import com.mafioso.model.Role;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Server-side validation and normalization for Milestone 4 student profiles,
 * applications, consultant assignments, and document reviews.
 */
public final class ConsultancyValidationUtil {

    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\p{Cntrl}&&[^\\r\\n\\t]]");
    private static final Pattern PASSPORT_PATTERN = Pattern.compile("^[A-Za-z0-9\\-\\s]{5,30}$");
    private static final Set<String> ALLOWED_CURRENCIES = Collections.unmodifiableSet(
            new HashSet<String>(Arrays.asList("GBP", "USD", "BDT", "EUR"))
    );

    private ConsultancyValidationUtil() {
    }

    public static String normalizeText(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static boolean hasUnsafeContent(String value) {
        if (value == null) {
            return false;
        }
        return CONTROL_CHARS.matcher(value).find() || value.contains("<") || value.contains(">");
    }

    public static LocalDate parseOptionalDate(String raw, String field, Map<String, String> errors) {
        String clean = normalizeText(raw);
        if (clean == null) {
            return null;
        }
        try {
            return LocalDate.parse(clean);
        } catch (DateTimeParseException ex) {
            errors.put(field, "Enter a valid date in YYYY-MM-DD format.");
            return null;
        }
    }

    public static Map<String, String> validateStudentProfile(StudentProfileRequest req) {
        Map<String, String> errors = new LinkedHashMap<String, String>();
        if (req == null) {
            errors.put("request", "Profile payload is required.");
            return errors;
        }

        String fullName = ValidationUtil.normalizeFullName(req.getFullName());
        if (fullName.isEmpty()) {
            errors.put("fullName", "Full name is required.");
        } else if (!ValidationUtil.isValidFullName(fullName)) {
            errors.put("fullName", "Full name must be between 2 and 120 valid characters.");
        }

        String phone = ValidationUtil.normalizePhoneNumber(req.getPhoneNumber());
        if (!ValidationUtil.isValidPhoneNumber(phone)) {
            errors.put("phoneNumber", "Enter a valid phone number (7 to 15 digits).");
        }

        LocalDate dob = parseOptionalDate(req.getDateOfBirth(), "dateOfBirth", errors);
        if (dob != null) {
            LocalDate now = LocalDate.now();
            if (!dob.isBefore(now) || dob.isAfter(now.minusYears(14)) || dob.isBefore(now.minusYears(100))) {
                errors.put("dateOfBirth", "Date of birth must represent an age between 14 and 100 years.");
            }
        }

        validateOptionalText(req.getGender(), "gender", 32, errors);
        validateOptionalText(req.getNationality(), "nationality", 80, errors);
        validateOptionalText(req.getPresentAddress(), "presentAddress", 500, errors);
        validateOptionalText(req.getPermanentAddress(), "permanentAddress", 500, errors);

        String passportNumber = normalizeText(req.getPassportNumber());
        if (passportNumber != null && !PASSPORT_PATTERN.matcher(passportNumber).matches()) {
            errors.put("passportNumber", "Passport number must be 5 to 30 alphanumeric characters.");
        }
        LocalDate issueDate = parseOptionalDate(req.getPassportIssueDate(), "passportIssueDate", errors);
        LocalDate expiryDate = parseOptionalDate(req.getPassportExpiryDate(), "passportExpiryDate", errors);
        if (issueDate != null && issueDate.isAfter(LocalDate.now())) {
            errors.put("passportIssueDate", "Passport issue date cannot be in the future.");
        }
        if (issueDate != null && expiryDate != null && !expiryDate.isAfter(issueDate)) {
            errors.put("passportExpiryDate", "Passport expiry date must be after the issue date.");
        }
        validateOptionalText(req.getPassportIssuingCountry(), "passportIssuingCountry", 80, errors);

        validateOptionalText(req.getHighestQualification(), "highestQualification", 100, errors);
        validateOptionalText(req.getSscResult(), "sscResult", 80, errors);
        validateOptionalText(req.getHscResult(), "hscResult", 80, errors);
        validateOptionalText(req.getBachelorInstitution(), "bachelorInstitution", 160, errors);
        validateOptionalText(req.getBachelorSubject(), "bachelorSubject", 120, errors);
        validateOptionalText(req.getBachelorCgpa(), "bachelorCgpa", 40, errors);

        if (req.getBachelorGraduationYear() != null) {
            int year = req.getBachelorGraduationYear().intValue();
            int maxYear = Year.now().getValue() + 6;
            if (year < 1960 || year > maxYear) {
                errors.put("bachelorGraduationYear", "Graduation year must be between 1960 and " + maxYear + ".");
            }
        }

        validateOptionalText(req.getIeltsScore(), "ieltsScore", 32, errors);
        validateOptionalText(req.getPteScore(), "pteScore", 32, errors);
        validateOptionalText(req.getToeflScore(), "toeflScore", 32, errors);
        validateOptionalText(req.getOtherEnglishQualification(), "otherEnglishQualification", 160, errors);

        String degreeRaw = normalizeText(req.getPreferredDegreeLevel());
        if (degreeRaw != null) {
            try {
                DegreeLevel.fromString(degreeRaw);
            } catch (IllegalArgumentException ex) {
                errors.put("preferredDegreeLevel", "Preferred degree level must be BSc or MSc.");
            }
        }
        validateOptionalText(req.getPreferredSubject(), "preferredSubject", 150, errors);
        validateOptionalText(req.getPreferredUniversity(), "preferredUniversity", 180, errors);
        validateOptionalText(req.getPreferredIntake(), "preferredIntake", 60, errors);
        validateOptionalText(req.getPreferredStudyLocation(), "preferredStudyLocation", 120, errors);

        String budgetRaw = normalizeText(req.getEstimatedBudget());
        if (budgetRaw != null) {
            try {
                BigDecimal budget = new BigDecimal(budgetRaw.replace(",", ""));
                if (budget.compareTo(BigDecimal.ZERO) < 0 || budget.compareTo(new BigDecimal("10000000")) > 0) {
                    errors.put("estimatedBudget", "Estimated budget must be between 0 and 10,000,000.");
                }
            } catch (NumberFormatException ex) {
                errors.put("estimatedBudget", "Estimated budget must be a valid numeric amount.");
            }
        }

        String currencyRaw = normalizeText(req.getBudgetCurrency());
        if (currencyRaw != null && !ALLOWED_CURRENCIES.contains(currencyRaw.toUpperCase(Locale.ROOT))) {
            errors.put("budgetCurrency", "Currency must be GBP, USD, BDT, or EUR.");
        }

        validateOptionalText(req.getFundingSource(), "fundingSource", 80, errors);
        validateOptionalText(req.getSponsorInfo(), "sponsorInfo", 500, errors);

        return errors;
    }

    public static Map<String, String> validateCreateApplication(CreateApplicationRequest req) {
        Map<String, String> errors = new LinkedHashMap<String, String>();
        if (req == null) {
            errors.put("request", "Application payload is required.");
            return errors;
        }

        String degreeRaw = normalizeText(req.getDegreeLevel());
        if (degreeRaw == null) {
            errors.put("degreeLevel", "Degree level (BSc or MSc) is required.");
        } else {
            try {
                DegreeLevel.fromString(degreeRaw);
            } catch (IllegalArgumentException ex) {
                errors.put("degreeLevel", "Degree level must be BSc or MSc.");
            }
        }

        validateRequiredText(req.getPreferredSubject(), "preferredSubject", "Preferred subject", 2, 150, errors);
        validateRequiredText(req.getIntendedIntake(), "intendedIntake", "Intended intake", 3, 60, errors);
        validateRequiredText(req.getPreferredUniversity(), "preferredUniversity", "Preferred university", 2, 180, errors);
        validateOptionalText(req.getPreferredLocation(), "preferredLocation", 120, errors);
        validateOptionalText(req.getPersonalStatement(), "personalStatement", 2000, errors);

        return errors;
    }

    public static Map<String, String> validateStatusUpdate(UpdateApplicationStatusRequest req, Role actorRole) {
        Map<String, String> errors = new LinkedHashMap<String, String>();
        if (req == null) {
            errors.put("request", "Status update payload is required.");
            return errors;
        }

        String rawStatus = normalizeText(req.getStatus());
        if (rawStatus == null) {
            errors.put("status", "Target application status is required.");
        } else {
            try {
                ApplicationStatus target = ApplicationStatus.fromString(rawStatus);
                if (actorRole == Role.CONSULTANT && !target.isConsultantAllowedTarget()) {
                    errors.put("status", "Consultants may update progress to UNDER_REVIEW, DOCUMENTS_PENDING, DOCUMENTS_REVIEW, IN_PROGRESS, or COMPLETED.");
                }
            } catch (IllegalArgumentException ex) {
                errors.put("status", "Invalid application status value.");
            }
        }

        validateOptionalText(req.getNote(), "note", 500, errors);
        return errors;
    }

    public static Map<String, String> validateAssignConsultant(AssignConsultantRequest req) {
        Map<String, String> errors = new LinkedHashMap<String, String>();
        if (req == null) {
            errors.put("request", "Assignment payload is required.");
            return errors;
        }
        if (req.getConsultantId() == null || req.getConsultantId().longValue() <= 0L) {
            errors.put("consultantId", "Please select a valid consultant.");
        }
        validateOptionalText(req.getNote(), "note", 500, errors);
        return errors;
    }

    public static Map<String, String> validateDocumentReview(ReviewDocumentRequest req) {
        Map<String, String> errors = new LinkedHashMap<String, String>();
        if (req == null) {
            errors.put("request", "Document review payload is required.");
            return errors;
        }

        DocumentReviewStatus parsedStatus = null;
        String rawStatus = normalizeText(req.getReviewStatus());
        if (rawStatus == null) {
            errors.put("reviewStatus", "Review status is required.");
        } else {
            try {
                parsedStatus = DocumentReviewStatus.fromString(rawStatus);
            } catch (IllegalArgumentException ex) {
                errors.put("reviewStatus", "Review status must be PENDING, UNDER_REVIEW, APPROVED, or REJECTED.");
            }
        }

        String cleanNote = normalizeText(req.getReviewNote());
        if (parsedStatus == DocumentReviewStatus.REJECTED && cleanNote == null) {
            errors.put("reviewNote", "Please provide a review note explaining what needs correction.");
        }
        validateOptionalText(cleanNote, "reviewNote", 500, errors);
        return errors;
    }

    private static void validateRequiredText(String raw,
                                             String field,
                                             String label,
                                             int minLen,
                                             int maxLen,
                                             Map<String, String> errors) {
        String clean = normalizeText(raw);
        if (clean == null) {
            errors.put(field, label + " is required.");
            return;
        }
        if (clean.length() < minLen || clean.length() > maxLen) {
            errors.put(field, label + " must be between " + minLen + " and " + maxLen + " characters.");
            return;
        }
        if (hasUnsafeContent(clean)) {
            errors.put(field, label + " contains invalid characters.");
        }
    }

    private static void validateOptionalText(String raw, String field, int maxLen, Map<String, String> errors) {
        String clean = normalizeText(raw);
        if (clean == null) {
            return;
        }
        if (clean.length() > maxLen) {
            errors.put(field, "Must not exceed " + maxLen + " characters.");
            return;
        }
        if (hasUnsafeContent(clean)) {
            errors.put(field, "Contains invalid characters.");
        }
    }
}
