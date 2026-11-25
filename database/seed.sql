-- =============================================================================
-- MAFIOSO Initial Seed Data (Milestone 1 Foundation)
-- Idempotent seed script with non-sensitive platform metadata only.
-- =============================================================================

INSERT INTO schema_migrations (version, description, script_name)
VALUES ('V001', 'Initial Milestone 1 foundation tables', 'V001__initial_foundation.sql')
ON DUPLICATE KEY UPDATE
    description = VALUES(description),
    script_name = VALUES(script_name);

INSERT INTO system_metadata (meta_key, meta_value)
VALUES
    ('platform.name', 'MAFIOSO'),
    ('platform.milestone', '1'),
    ('platform.target_corridor', 'Bangladesh -> United Kingdom (BSc / MSc)')
ON DUPLICATE KEY UPDATE
    meta_value = VALUES(meta_value);
