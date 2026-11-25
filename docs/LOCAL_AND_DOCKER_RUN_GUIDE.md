# MAFIOSO Run Guide: Turning Docker On, Turning Docker Off, and Running Locally Without Docker

This guide explains how to start and stop the MAFIOSO Docker Compose stack safely, and how to run the entire platform locally on your machine without Docker (MySQL 8 + Java 8 / Tomcat 9 + React / Vite).

---

## Quick Comparison

| Mode | Entry URL | Backend | Database | Data Persistence |
| --- | --- | --- | --- | --- |
| **Docker Compose** | `http://localhost/` (or `:${APP_HTTP_PORT}`) | Tomcat 9 in `backend` container (`:8080` internal) | MySQL 8 in `db` container (`:3306` internal) | Named volume `mafioso_mysql_data` |
| **Local (No Docker)** | `http://localhost:5173/` (Vite dev proxy) | Local Tomcat 9 on `:8080` | Local MySQL 8 on `127.0.0.1:3306` | Local host MySQL data directory |

---

## 1. How to Turn Docker ON

Run all commands from the project root directory (`MAFIOSO/`, where `compose.yaml` is located).

### Step 1: Ensure Docker Engine Is Running
- **Windows / macOS:** Open **Docker Desktop** and wait until it shows **Engine running**.
- **Linux:** Ensure the Docker daemon is active:
  ```bash
  sudo systemctl start docker
  ```

### Step 2: Ensure `.env` Exists (First-Time Setup Only)
If you have not created `.env` yet:
- **Windows (PowerShell):**
  ```powershell
  Copy-Item .env.example .env
  ```
- **macOS / Linux:**
  ```bash
  cp .env.example .env
  ```
Open `.env` and ensure `DB_PASSWORD` and `DB_ROOT_PASSWORD` are set (keep `DB_HOST=db` and `DB_PORT=3306` when using Docker).

### Step 3: Start the Containers
- **Normal start** (after Docker was stopped or after restarting your PC, when code has not changed):
  ```bash
  docker compose up -d
  ```
- **Build and start** (first-time setup, or whenever you pulled/edited Java, React, SQL, or Docker files):
  ```bash
  docker compose up --build -d
  ```
- **Rebuild only one service** after targeted edits:
  ```bash
  docker compose up --build -d backend
  docker compose up --build -d frontend
  ```

### Step 4: Verify Containers Are Healthy
```bash
docker compose ps
```
Wait until `mafioso-db-1`, `mafioso-backend-1`, and `mafioso-frontend-1` all show `Up` and `(healthy)`.

Open in your browser:
- Landing Page: `http://localhost/`
- System Health Diagnostics: `http://localhost/health`
- Sign In / Register: `http://localhost/login` and `http://localhost/register`

*(Optional log streaming: `docker compose logs -f` or `docker compose logs -f backend`.)*

---

## 2. How to Turn Docker OFF

Choose between **stopping containers** (`stop`) or **stopping and removing containers** (`down`). Both keep your MySQL database and uploaded documents safe.

### Option A: Standard Clean Shutdown (Recommended)
Stops and removes the running containers and temporary bridge network while **preserving** the `mafioso_mysql_data` and `mafioso_student_documents` volumes:
```bash
docker compose down
```
To turn the stack back on later:
```bash
docker compose up -d
```

### Option B: Pause / Stop Without Removing Containers
Stops the container processes in place for fast restart:
```bash
docker compose stop
```
To resume later:
```bash
docker compose start
```

> **CRITICAL WARNING — Never Use `-v` Unless You Want Data Erased:**
> Do **not** run `docker compose down -v`. The `-v` flag deletes the persistent Docker volumes (`mafioso_mysql_data` and `mafioso_student_documents`), permanently wiping all user accounts, migrations, and stored files.

*(On Windows/macOS, once `docker compose down` finishes, you may also quit Docker Desktop from the system tray if you want to free RAM.)*

---

## 3. How to Run Without Docker Locally

If someone wants to develop or run MAFIOSO directly on their machine without Docker, they run three local processes: **MySQL 8** (`:3306`), **Apache Tomcat 9** (`:8080`), and **Vite React Dev Server** (`:5173`).

> **Port Conflict Note:** MAFIOSO's Docker setup never exposes MySQL port `3306` or Tomcat port `8080` to your host, so local MySQL/Tomcat will not conflict with Docker containers. If Docker is running on port `80`, local Vite (`5173`) and Tomcat (`8080`) still use separate ports, though shutting down Docker (`docker compose down`) first keeps resource usage lower and avoids confusion over which database you are using.

### Prerequisites (Local Machine)
1. **Java JDK 8 (`1.8`)** — e.g., Eclipse Temurin 8 (`java -version` must support Java 8 compilation/runtime).
2. **Apache Maven 3.9+** (`mvn -version`).
3. **Apache Tomcat 9 (`9.0.x`)** — **Must be Tomcat 9**, **NOT Tomcat 10 or 11**. MAFIOSO uses Servlet 4.0 (`javax.servlet.*`); Tomcat 10+ uses `jakarta.servlet.*` and will fail to deploy `ROOT.war`.
4. **MySQL 8.0** running locally on `127.0.0.1:3306`.
5. **Node.js 22+ & npm** (`node -v`, `npm -v`).

---

### Step A: Prepare Local MySQL 8 Database
Connect to your local MySQL server as `root` and create the `mafioso_db` schema and dedicated user:

```sql
CREATE DATABASE IF NOT EXISTS mafioso_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'mafioso_user'@'localhost'
  IDENTIFIED BY 'replace_with_local_db_password';

GRANT ALL PRIVILEGES ON mafioso_db.* TO 'mafioso_user'@'localhost';
FLUSH PRIVILEGES;
```

Apply the cumulative schema and non-sensitive seed metadata from the repository root:

- **macOS / Linux (Bash/Zsh):**
  ```bash
  mysql -h 127.0.0.1 -u mafioso_user -p mafioso_db < database/schema.sql
  mysql -h 127.0.0.1 -u mafioso_user -p mafioso_db < database/seed.sql
  ```
- **Windows (PowerShell):**
  ```powershell
  Get-Content database\schema.sql -Raw | mysql -h 127.0.0.1 -u mafioso_user -p mafioso_db
  Get-Content database\seed.sql -Raw | mysql -h 127.0.0.1 -u mafioso_user -p mafioso_db
  ```

*(Note: When Tomcat starts, `DatabaseMigrationRunner` also checks `schema_migrations` automatically and applies any pending migrations safely.)*

---

### Step B: Build the Java 8 Backend (`ROOT.war`)
From `MAFIOSO/backend`:
```bash
cd backend
mvn -B clean test package
```
Verify the build outputs `backend/target/ROOT.war` and passes all JUnit tests.

---

### Step C: Configure & Start Apache Tomcat 9
1. **Clean default ROOT webapp in Tomcat 9:**
   Delete `<TOMCAT9_HOME>/webapps/ROOT` and `<TOMCAT9_HOME>/webapps/ROOT.war` (if present), then copy `backend/target/ROOT.war` to `<TOMCAT9_HOME>/webapps/ROOT.war`.
   *(Deploying as `ROOT.war` is required so API routes are served at `/api/v1/...` and `/ws/...` without a context prefix.)*

2. **Create a local document storage directory:**
   - Windows example: `D:\MAFIOSO\storage\documents`
   - macOS / Linux example: `/tmp/mafioso/storage/documents` (or `/var/mafioso/storage/documents`)

3. **Provide required environment variables to Tomcat 9:**
   Create `<TOMCAT9_HOME>/bin/setenv.bat` (Windows) or `<TOMCAT9_HOME>/bin/setenv.sh` (macOS/Linux):

   - **Windows (`<TOMCAT9_HOME>\bin\setenv.bat`):**
     ```bat
     set "APP_ENV=development"
     set "DB_HOST=127.0.0.1"
     set "DB_PORT=3306"
     set "DB_NAME=mafioso_db"
     set "DB_USER=mafioso_user"
     set "DB_PASSWORD=replace_with_local_db_password"
     set "DB_USE_SSL=false"
     set "STORAGE_UPLOAD_DIR=D:\MAFIOSO\storage\documents"
     set "SESSION_TIMEOUT_MINUTES=30"
     ```

   - **macOS / Linux (`<TOMCAT9_HOME>/bin/setenv.sh`, then `chmod +x setenv.sh`):**
     ```sh
     export APP_ENV=development
     export DB_HOST=127.0.0.1
     export DB_PORT=3306
     export DB_NAME=mafioso_db
     export DB_USER=mafioso_user
     export DB_PASSWORD=replace_with_local_db_password
     export DB_USE_SSL=false
     export STORAGE_UPLOAD_DIR=/tmp/mafioso/storage/documents
     export SESSION_TIMEOUT_MINUTES=30
     ```

4. **Start Tomcat 9 on port `8080`:**
   - Windows: `<TOMCAT9_HOME>\bin\catalina.bat run`
   - macOS / Linux: `<TOMCAT9_HOME>/bin/catalina.sh run`

5. **Verify backend APIs directly:**
   - `http://localhost:8080/api/v1/health` → should return `"status":"UP"`
   - `http://localhost:8080/api/v1/health/database` → should return `"status":"UP"` and `"schemaVersion":"V002"`

---

### Step D: Start the React Frontend (Vite Dev Server)
`frontend/vite.config.js` is preconfigured to listen on port `5173` and proxy all `/api` and `/ws` calls same-origin to `http://localhost:8080` so `JSESSIONID` cookies (`HttpOnly`, `SameSite=Lax`) and `X-CSRF-Token` work seamlessly without CORS workarounds.

From `MAFIOSO/frontend`:
```bash
cd frontend
npm ci
npm run dev
```
Open `http://localhost:5173/` in your browser (and `http://localhost:5173/health` to confirm Backend REST API, Database Connectivity, and WebSocket Proxy all report `UP`).

*(To stop local services: press `Ctrl+C` in the Vite terminal and `Ctrl+C` in the Tomcat terminal, or run `<TOMCAT9_HOME>/bin/shutdown.bat` / `shutdown.sh`.)*

---

### Step E: Creating Admin & Consultant Accounts Locally Without Docker
Because `ADMIN` and `CONSULTANT` accounts cannot register via `/register`, run the CLI utilities against your unpacked Tomcat `ROOT` classes with the same database environment variables exported in your shell:

- **Windows (PowerShell):**
  ```powershell
  $env:DB_HOST = "127.0.0.1"
  $env:DB_PORT = "3306"
  $env:DB_NAME = "mafioso_db"
  $env:DB_USER = "mafioso_user"
  $env:DB_PASSWORD = "replace_with_local_db_password"
  $env:DB_USE_SSL = "false"

  $TOMCAT = "C:\path\to\apache-tomcat-9"
  $CP = "$TOMCAT\webapps\ROOT\WEB-INF\classes;$TOMCAT\webapps\ROOT\WEB-INF\lib\*"

  # Initial Administrator (one-time)
  java -cp $CP com.mafioso.bootstrap.AdminBootstrapCli

  # Provision Consultant
  java -cp $CP com.mafioso.bootstrap.ConsultantProvisionCli
  ```

- **macOS / Linux (Bash/Zsh):**
  ```bash
  export DB_HOST=127.0.0.1
  export DB_PORT=3306
  export DB_NAME=mafioso_db
  export DB_USER=mafioso_user
  export DB_PASSWORD=replace_with_local_db_password
  export DB_USE_SSL=false

  TOMCAT=/path/to/apache-tomcat-9
  CP="$TOMCAT/webapps/ROOT/WEB-INF/classes:$TOMCAT/webapps/ROOT/WEB-INF/lib/*"

  # Initial Administrator (one-time)
  java -cp "$CP" com.mafioso.bootstrap.AdminBootstrapCli

  # Provision Consultant
  java -cp "$CP" com.mafioso.bootstrap.ConsultantProvisionCli
  ```

*(Note the classpath separator difference: `;` on Windows vs `:` on macOS/Linux.)*
