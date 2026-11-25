-- =============================================================================
-- MAFIOSO Initial Database Schema (Milestone 1 Foundation)
-- Minimal schema only. Business domain tables will be added in Milestone 2+.
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
