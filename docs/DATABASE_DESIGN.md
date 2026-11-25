# MAFIOSO Database Design & Strategy

## 1. Engine & Encoding

- **RDBMS:** MySQL `8.0.46` (`mysql:8.0.46-oraclelinux9`)
- **Database Name:** `mafioso_db`
- **Character Set / Collation:** `utf8mb4` / `utf8mb4_unicode_ci`
- **Network Access:** Exposed only inside the private `mafioso_internal` Docker network (`db:3306`). No host port mapping is published.

## 2. Milestone 1 Minimal Schema

Per Milestone 1 scope, domain business tables (users, universities, applications, payments, documents) are deferred to subsequent milestones. Only foundational metadata tables are created:

### `schema_migrations`
Tracks applied forward-only SQL migrations (`version`, `description`, `script_name`, `checksum`, `applied_at`).

### `system_metadata`
Stores non-sensitive platform bootstrap metadata (`meta_key`, `meta_value`, `updated_at`), seeded idempotently with platform name, milestone number, and Bangladesh-to-UK target corridor.

## 3. Initialization & Migration Strategy

- `database/init/01-init-mafioso.sh`: Executed automatically by MySQL's `/docker-entrypoint-initdb.d/` on first boot when `mafioso_mysql_data` is empty. Applies `database/schema.sql` and `database/seed.sql`.
- `database/migrations/`: Houses forward-only versioned SQL scripts (`V001__initial_foundation.sql`, followed by `V002__...`, etc. in future milestones).

## 4. Persistence & Backup Strategy

- Database data persists in the named Docker volume `mafioso_mysql_data`.
- Standard shutdown (`docker compose down`) preserves all data.
- **Never run `docker compose down -v`** in environments where data must be retained, as `-v` removes named volumes.

### Backup & Restore Example

```bash
# Create compressed SQL backup from running db container
docker compose exec -T db sh -c 'mysqldump -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE"' | gzip > mafioso_backup_$(date +%F).sql.gz

# Restore from SQL backup
gunzip -c mafioso_backup_YYYY-MM-DD.sql.gz | docker compose exec -T db sh -c 'mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE"'
```
