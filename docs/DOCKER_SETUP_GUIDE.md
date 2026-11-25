# Running MAFIOSO on Another PC Using Docker

This guide covers every step needed to clone, configure, start, verify, and troubleshoot **MAFIOSO** on a new machine (Windows, macOS, or Linux) using Docker—without installing Java, Maven, Tomcat, Node.js, or MySQL locally.

---

## 1. Prerequisites on the New Machine

You only need two tools installed:

1. **Git**
2. **Docker with Compose v2+**
   - **Windows / macOS:** [Docker Desktop](https://www.docker.com/products/docker-desktop/) (ensure Docker Desktop is **open and running** before running commands; on Windows, enable WSL 2 backend).
   - **Linux:** Docker Engine + `docker-compose-plugin` (ensure the Docker daemon is running: `sudo systemctl enable --now docker`).

> **Architecture Compatibility:** All pinned Docker images (`node:22.23.5-alpine3.23`, `nginx:1.28.3-alpine3.23`, `maven:3.9.9-eclipse-temurin-8`, `tomcat:9.0.124-jdk8-temurin-noble`, and `mysql:8.0.46-oraclelinux9`) support both `linux/amd64` (Intel/AMD PCs) and `linux/arm64` (Apple Silicon M-series Macs and ARM Linux).

You do **not** need local JDK 8, Maven, Tomcat 9, Node.js, npm, or MySQL installed—and MAFIOSO will not interfere with any existing Tomcat or MySQL installation on the machine.

---

## 2. Step-by-Step Setup

### Step 1: Clone the Repository
> **Windows line-ending note:** `.gitattributes` enforces `LF` line endings automatically on clone so Linux shell scripts (`database/init/01-init-mafioso.sh`) execute cleanly inside Docker. Avoid transferring the folder as a manually re-saved `.zip` from tools that convert line endings to `CRLF`.

```bash
git clone <repository-url>
cd MAFIOSO
```

### Step 2: Create Your `.env` File
`.env` is intentionally excluded by `.gitignore`, so it will **not** exist after cloning. Create it from `.env.example`:

- **Windows (PowerShell):**
  ```powershell
  Copy-Item .env.example .env
  ```
- **macOS / Linux (Bash/Zsh):**
  ```bash
  cp .env.example .env
  ```

Open `.env` in your editor and update the credential placeholders:

```dotenv
APP_ENV=development
APP_HTTP_PORT=80

DB_HOST=db
DB_PORT=3306
DB_NAME=mafioso_db
DB_USER=mafioso_user
DB_PASSWORD=replace_with_a_strong_password
DB_ROOT_PASSWORD=replace_with_a_strong_root_password
DB_USE_SSL=false

STORAGE_UPLOAD_DIR=/var/mafioso/storage/documents

VITE_APP_NAME=MAFIOSO
VITE_API_BASE_URL=
```

**Important rules for `.env`:**
- Keep `DB_HOST=db` and `DB_PORT=3306` exactly as shown. Do **not** change `DB_HOST` to `localhost`.
- Replace `DB_PASSWORD` and `DB_ROOT_PASSWORD` with real passwords.
- Make sure the file is named exactly `.env` (not `.env.txt` on Windows).
- If port `80` is already used on the new PC (for example by IIS, Apache, or another local proxy), change `APP_HTTP_PORT=80` to a free port such as `APP_HTTP_PORT=8088`.

### Step 3: Build Images and Start Containers
Run from the project root directory (the folder containing `compose.yaml`):

```bash
docker compose up --build -d
```

What happens automatically on the first run:
1. Pulls `mysql:8.0.46-oraclelinux9` and initializes `mafioso_db` with `database/schema.sql` and `database/seed.sql`.
2. Compiles the Java 8 Maven WAR (`ROOT.war`) inside `maven:3.9.9-eclipse-temurin-8` and deploys it into `tomcat:9.0.124-jdk8-temurin-noble`.
3. Builds the React Vite bundle inside `node:22.23.5-alpine3.23` and configures `nginx:1.28.3-alpine3.23` to serve the SPA and reverse-proxy `/api/` and `/ws/`.

*(First-time image pulls and builds typically take 2–5 minutes depending on internet speed; subsequent rebuilds use Docker layer caching and finish in seconds.)*

### Step 4: Verify Everything Is Healthy
Check container states:

```bash
docker compose ps
```

All three containers should show `Up` and `(healthy)`:
- `mafioso-db-1` (internal `3306/tcp` only)
- `mafioso-backend-1` (internal `8080/tcp` only)
- `mafioso-frontend-1` (`0.0.0.0:80->80/tcp`, or your chosen `APP_HTTP_PORT`)

Open in a browser (adjust port if you changed `APP_HTTP_PORT`):
- **Landing page:** `http://localhost/`
- **Live diagnostics page:** `http://localhost/health`
  - Confirms **Frontend Runtime**, **Backend REST API** (`/api/v1/health`), **Database Connectivity** (`/api/v1/health/database`), and **WebSocket Proxy** (`/ws/health`) all show `UP`.

---

## 3. Common Day-to-Day Docker Commands

```bash
# View live logs across all services
docker compose logs -f

# View logs for a single service (db, backend, or frontend)
docker compose logs -f backend

# Rebuild only after editing frontend code
docker compose up --build -d frontend

# Rebuild only after editing Java backend code
docker compose up --build -d backend

# Stop containers safely while KEEPING database and uploaded document data
docker compose down

# Start previously built containers again
docker compose up -d
```

> **CRITICAL WARNING — Data Loss:**
> Never run `docker compose down -v` on a machine whose data you want to keep. The `-v` flag deletes the named persistent volumes (`mafioso_mysql_data` and `mafioso_student_documents`), permanently erasing the MySQL database and any stored student documents.

---

## 4. Troubleshooting Common New-PC Issues

### Issue 1: `Bind for 0.0.0.0:80 failed: port is already allocated`
Another program (IIS, Apache, Skype, or another Docker container) is using port `80` on that PC.
- **Fix:** Edit `.env` and set `APP_HTTP_PORT=8088` (or any free port), then run:
  ```bash
  docker compose up -d frontend
  ```
  Access the app at `http://localhost:8088`.

### Issue 2: `required variable DB_PASSWORD is missing` or `DB_NAME must be set in .env`
- **Cause:** `.env` was not created yet, was saved in the wrong folder, or Windows Notepad named it `.env.txt`.
- **Fix:** Ensure `.env` exists directly alongside `compose.yaml` (`MAFIOSO/.env`) and contains `DB_NAME`, `DB_USER`, `DB_PASSWORD`, and `DB_ROOT_PASSWORD`.

### Issue 3: `Cannot connect to the Docker daemon` / `dockerDesktopLinuxEngine` error
- **Cause:** Docker Desktop (Windows/macOS) or the Docker service (Linux) is not started.
- **Fix:** Open Docker Desktop and wait until the bottom status indicator says **Engine running**, or on Linux run `sudo systemctl start docker`.

### Issue 4: Database login fails after changing `DB_PASSWORD` in `.env`
- **Cause:** MySQL only applies `MYSQL_USER` / `MYSQL_PASSWORD` / `database/init/` when creating `mafioso_mysql_data` for the **very first time**. Changing `.env` passwords afterward does not update an already-initialized volume.
- **Fix (only if there is no valuable data yet):** Reset the volume cleanly and reinitialize:
  ```bash
  docker compose down -v
  docker compose up --build -d
  ```

### Issue 5: `01-init-mafioso.sh: no such file or directory` or `\r` error in `db` logs
- **Cause:** Files were copied via a non-Git tool that converted `LF` line endings to Windows `CRLF`.
- **Fix:** Clone directly via Git (`.gitattributes` enforces `LF`), or convert `database/init/01-init-mafioso.sh` back to `LF` (`\n`), then reset the fresh DB volume (`docker compose down -v && docker compose up --build -d`).

### Issue 6: Connecting MySQL Workbench on a PC that already runs local MySQL (`3306`)
Because MAFIOSO does not publish MySQL port `3306` on the host, it never conflicts with an existing local MySQL server. To inspect `mafioso_db`:
- **CLI inside container:**
  ```bash
  docker compose exec db sh -c 'mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE"'
  ```
- **Temporary localhost-only port bridge for MySQL Workbench (port `3307`):**
  ```bash
  docker run --rm --network mafioso_mafioso_internal -p 127.0.0.1:3307:3306 alpine/socat TCP-LISTEN:3306,fork,reuseaddr TCP:db:3306
  ```
  Then connect MySQL Workbench to `127.0.0.1`, port `3307`, username from `DB_USER`, password from `DB_PASSWORD`, schema `mafioso_db`.

### Issue 7: Build fails midway due to slow or unstable internet
- **Fix:** Check network/proxy access for Maven Central, npm registry, and Docker Hub, then retry cleanly:
  ```bash
  docker compose build --no-cache
  docker compose up -d
  ```

---

## 5. Migrating Database Data From Old PC to New PC

If you already have data in `mafioso_db` on one PC and want to move it to the new PC:

1. **On the original PC (export SQL dump):**
   ```bash
   docker compose exec -T db sh -c 'mysqldump -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE"' > mafioso_backup.sql
   ```
2. Copy `mafioso_backup.sql` to the new PC after completing Section 2 (`docker compose up --build -d`).
3. **On the new PC (import SQL dump):**
   - Bash / Zsh:
     ```bash
     docker compose exec -T db sh -c 'mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE"' < mafioso_backup.sql
     ```
   - PowerShell:
     ```powershell
     Get-Content mafioso_backup.sql -Raw | docker compose exec -T db sh -c 'mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE"'
     ```
