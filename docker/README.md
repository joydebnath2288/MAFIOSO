# MAFIOSO Docker Infrastructure Guide

## Container Services

`compose.yaml` defines three application services isolated inside a private Docker bridge network (`mafioso_internal`):

1. **`frontend`**
   - Build stage: `node:22.23.5-alpine3.23` (`npm ci && npm run build`)
   - Runtime stage: `nginx:1.28.3-alpine3.23`
   - Exposes host HTTP port `${APP_HTTP_PORT:-80}:80` (the only host-exposed port)
   - Serves React SPA static assets, falls back to `/index.html` for React Router, proxies `/api/` to `backend:8080`, and upgrades `/ws/` WebSocket connections to `backend:8080`.
2. **`backend`**
   - Build stage: `maven:3.9.9-eclipse-temurin-8` (`mvn -B clean package`)
   - Runtime stage: `tomcat:9.0.124-jdk8-temurin-noble`
   - Deploys `ROOT.war` to `/usr/local/tomcat/webapps/ROOT.war`
   - Runs Servlet 4.0 REST APIs and JSR-356 WebSocket endpoints in a single container on internal port `8080`
   - Mounts persistent private document storage at `/var/mafioso/storage/documents`
3. **`db`**
   - Image: `mysql:8.0.46-oraclelinux9`
   - Internal port `3306` only (`expose`, never published to the host)
   - Runs initialization scripts in `database/init/` on first startup when `mafioso_mysql_data` is empty
   - Includes MySQL `SELECT 1` healthcheck before `backend` starts

## Named Persistent Volumes

- `mafioso_mysql_data`: Stores MySQL 8 database files (`/var/lib/mysql`).
- `mafioso_student_documents`: Private storage for future student document uploads (`/var/mafioso/storage/documents`). Uploaded files are never baked into Docker images or served publicly by Nginx.

## Common Operations

```bash
# Build images and start all services in background
docker compose up --build -d

# Check service status and healthchecks
docker compose ps

# Stream live container logs
docker compose logs -f

# Stop and remove containers while PRESERVING volumes and data
docker compose down
```

> **WARNING — Data Loss Risk:**
> Running `docker compose down -v` **deletes all named volumes** (`mafioso_mysql_data` and `mafioso_student_documents`), permanently destroying the MySQL database and any stored student documents. Never use `-v` unless you intentionally want a clean reset.
