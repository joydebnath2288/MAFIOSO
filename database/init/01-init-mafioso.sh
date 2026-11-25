#!/usr/bin/env bash
# Executed automatically by the official MySQL 8 container on first initialization
# when the persistent /var/lib/mysql data volume is empty.
(
    set -euo pipefail
    echo "[MAFIOSO DB Init] Applying schema.sql and seed.sql to '${MYSQL_DATABASE}'..."
    mysql --protocol=socket -uroot -p"${MYSQL_ROOT_PASSWORD}" "${MYSQL_DATABASE}" < /opt/mafioso/db/schema.sql
    mysql --protocol=socket -uroot -p"${MYSQL_ROOT_PASSWORD}" "${MYSQL_DATABASE}" < /opt/mafioso/db/seed.sql
    echo "[MAFIOSO DB Init] Initialization complete."
)
