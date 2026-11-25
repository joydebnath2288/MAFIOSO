# MAFIOSO Database Migration Strategy

MAFIOSO uses forward-only, sequentially versioned SQL migrations stored in `database/migrations/`.

## Naming Convention

`V<NNN>__<short_snake_case_description>.sql`

Examples:
- `V001__initial_foundation.sql` (Milestone 1 baseline)
- Future milestones: `V002__users_and_roles.sql`, `V003__universities_and_scholarships.sql`, etc.

## Rules

1. Never edit an already-applied migration script; create a new incremental `V<NNN>` script.
2. Track every applied script inside `schema_migrations` (`version`, `description`, `script_name`, `applied_at`).
3. Keep `database/schema.sql` aligned with the cumulative latest state for fresh container initialization.
4. Always back up the `mafioso_mysql_data` volume before applying destructive migrations in production.
