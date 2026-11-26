-- =============================================================================
-- MAFIOSO Cumulative Database Schema (Milestone 4: Consultancy Workflow)
-- Aligned with V001, V002, and V003 migrations.
-- =============================================================================

CREATE TABLE IF NOT EXISTS schema_migrations (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    version VARCHAR(32) NOT NULL,
    description VARCHAR(255) NOT NULL,
    script_name VARCHAR(255) NOT NULL,
    checksum VARCHAR(64) NULL,
    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_schema_migrations_version (version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS system_metadata (
    meta_key VARCHAR(64) NOT NULL,
    meta_value VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (meta_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(254) NOT NULL,
    phone_number VARCHAR(32) NULL,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP NULL DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email),
    KEY idx_users_role_status (role, status),
    KEY idx_users_created_at (created_at),
    CONSTRAINT chk_users_role CHECK (role IN ('STUDENT', 'CONSULTANT', 'ADMIN')),
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE', 'DISABLED', 'SUSPENDED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS student_profiles (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    date_of_birth DATE NULL,
    gender VARCHAR(32) NULL,
    nationality VARCHAR(80) NULL,
    present_address VARCHAR(500) NULL,
    permanent_address VARCHAR(500) NULL,
    passport_number VARCHAR(40) NULL,
    passport_issue_date DATE NULL,
    passport_expiry_date DATE NULL,
    passport_issuing_country VARCHAR(80) NULL,
    highest_qualification VARCHAR(100) NULL,
    ssc_result VARCHAR(80) NULL,
    hsc_result VARCHAR(80) NULL,
    bachelor_institution VARCHAR(160) NULL,
    bachelor_subject VARCHAR(120) NULL,
    bachelor_cgpa VARCHAR(40) NULL,
    bachelor_graduation_year SMALLINT UNSIGNED NULL,
    ielts_score VARCHAR(32) NULL,
    pte_score VARCHAR(32) NULL,
    toefl_score VARCHAR(32) NULL,
    other_english_qualification VARCHAR(160) NULL,
    preferred_degree_level VARCHAR(10) NULL,
    preferred_subject VARCHAR(150) NULL,
    preferred_university VARCHAR(180) NULL,
    preferred_intake VARCHAR(60) NULL,
    preferred_study_location VARCHAR(120) NULL,
    estimated_budget DECIMAL(12, 2) NULL,
    budget_currency VARCHAR(10) NOT NULL DEFAULT 'GBP',
    funding_source VARCHAR(80) NULL,
    sponsor_info VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_student_profiles_user_id (user_id),
    CONSTRAINT fk_student_profiles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT chk_student_profiles_degree_level CHECK (preferred_degree_level IS NULL OR preferred_degree_level IN ('BSc', 'MSc')),
    CONSTRAINT chk_student_profiles_budget CHECK (estimated_budget IS NULL OR estimated_budget >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS application_reference_sequences (
    ref_year INT NOT NULL,
    last_seq BIGINT UNSIGNED NOT NULL DEFAULT 0,
    PRIMARY KEY (ref_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS applications (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    reference_number VARCHAR(32) NOT NULL,
    student_id BIGINT UNSIGNED NOT NULL,
    degree_level VARCHAR(10) NOT NULL,
    preferred_subject VARCHAR(150) NOT NULL,
    intended_intake VARCHAR(60) NOT NULL,
    preferred_university VARCHAR(180) NOT NULL,
    preferred_location VARCHAR(120) NULL,
    personal_statement TEXT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'SUBMITTED',
    assigned_consultant_id BIGINT UNSIGNED NULL,
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_applications_reference_number (reference_number),
    KEY idx_applications_student_id (student_id, created_at),
    KEY idx_applications_status (status, created_at),
    KEY idx_applications_consultant_id (assigned_consultant_id, status),
    KEY idx_applications_created_at (created_at),
    CONSTRAINT fk_applications_student FOREIGN KEY (student_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_applications_consultant FOREIGN KEY (assigned_consultant_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT chk_applications_degree_level CHECK (degree_level IN ('BSc', 'MSc')),
    CONSTRAINT chk_applications_status CHECK (status IN ('SUBMITTED', 'UNDER_REVIEW', 'ASSIGNED', 'DOCUMENTS_PENDING', 'DOCUMENTS_REVIEW', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS application_status_history (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    application_id BIGINT UNSIGNED NOT NULL,
    previous_status VARCHAR(32) NULL,
    new_status VARCHAR(32) NOT NULL,
    changed_by_user_id BIGINT UNSIGNED NOT NULL,
    note VARCHAR(500) NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_app_status_history_app_id (application_id, changed_at),
    KEY idx_app_status_history_changed_by (changed_by_user_id),
    CONSTRAINT fk_status_history_application FOREIGN KEY (application_id) REFERENCES applications (id) ON DELETE CASCADE,
    CONSTRAINT fk_status_history_changed_by FOREIGN KEY (changed_by_user_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT chk_status_history_prev CHECK (previous_status IS NULL OR previous_status IN ('SUBMITTED', 'UNDER_REVIEW', 'ASSIGNED', 'DOCUMENTS_PENDING', 'DOCUMENTS_REVIEW', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT chk_status_history_new CHECK (new_status IN ('SUBMITTED', 'UNDER_REVIEW', 'ASSIGNED', 'DOCUMENTS_PENDING', 'DOCUMENTS_REVIEW', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS consultant_assignments (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    application_id BIGINT UNSIGNED NOT NULL,
    student_id BIGINT UNSIGNED NOT NULL,
    consultant_id BIGINT UNSIGNED NOT NULL,
    assigned_by_user_id BIGINT UNSIGNED NOT NULL,
    action VARCHAR(24) NOT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    note VARCHAR(500) NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    unassigned_at TIMESTAMP NULL DEFAULT NULL,
    unassigned_by_user_id BIGINT UNSIGNED NULL,
    PRIMARY KEY (id),
    KEY idx_ca_application_active (application_id, is_active, assigned_at),
    KEY idx_ca_consultant_active (consultant_id, is_active, assigned_at),
    KEY idx_ca_student_active (student_id, is_active),
    CONSTRAINT fk_ca_application FOREIGN KEY (application_id) REFERENCES applications (id) ON DELETE CASCADE,
    CONSTRAINT fk_ca_student FOREIGN KEY (student_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_ca_consultant FOREIGN KEY (consultant_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_ca_assigned_by FOREIGN KEY (assigned_by_user_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_ca_unassigned_by FOREIGN KEY (unassigned_by_user_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT chk_ca_action CHECK (action IN ('ASSIGNED', 'REASSIGNED', 'REMOVED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS application_documents (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    application_id BIGINT UNSIGNED NULL,
    student_id BIGINT UNSIGNED NOT NULL,
    uploaded_by_user_id BIGINT UNSIGNED NOT NULL,
    category VARCHAR(32) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(128) NOT NULL,
    storage_relative_path VARCHAR(255) NOT NULL,
    detected_content_type VARCHAR(120) NOT NULL,
    file_size_bytes BIGINT UNSIGNED NOT NULL,
    sha256_checksum VARCHAR(64) NOT NULL,
    review_status VARCHAR(24) NOT NULL DEFAULT 'PENDING',
    review_note VARCHAR(500) NULL,
    reviewed_by_user_id BIGINT UNSIGNED NULL,
    reviewed_at TIMESTAMP NULL DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_app_docs_stored_filename (stored_filename),
    KEY idx_app_docs_student (student_id, created_at),
    KEY idx_app_docs_application (application_id, created_at),
    KEY idx_app_docs_review_status (review_status),
    CONSTRAINT fk_app_docs_application FOREIGN KEY (application_id) REFERENCES applications (id) ON DELETE SET NULL,
    CONSTRAINT fk_app_docs_student FOREIGN KEY (student_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_app_docs_uploaded_by FOREIGN KEY (uploaded_by_user_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_app_docs_reviewed_by FOREIGN KEY (reviewed_by_user_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT chk_app_docs_category CHECK (category IN ('PASSPORT', 'ACADEMIC_CERTIFICATE', 'ACADEMIC_TRANSCRIPT', 'ENGLISH_CERTIFICATE', 'CV', 'FINANCIAL_DOCUMENT', 'OTHER')),
    CONSTRAINT chk_app_docs_review_status CHECK (review_status IN ('PENDING', 'UNDER_REVIEW', 'APPROVED', 'REJECTED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
