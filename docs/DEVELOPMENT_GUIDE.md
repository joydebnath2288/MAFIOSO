# MAFIOSO Development & Deployment Guide

## 1. Prerequisites

To run the full containerized stack on any machine (Linux, macOS, or Windows with Docker Desktop):
- Git
- Docker Engine & Docker Compose v2+

Optional tools for local non-container builds and unit testing:
- JDK 8 (`1.8`) and Apache Maven 3.9+
- Node.js 22+ and npm

## 2. Running MAFIOSO on Another Machine

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd MAFIOSO
   ```

2. **Create environment configuration:**
   ```bash
   cp .env.example .env
   ```
   Open `.env` and set strong, non-default values for `DB_PASSWORD` and `DB_ROOT_PASSWORD`. Never commit `.env` to Git.

3. **Build and launch all services:**
   ```bash
   docker compose up --build -d
   ```

4. **Verify container health:**
   ```bash
   docker compose ps
   docker compose logs -f
   ```
   Wait until `db` and `backend` report `(healthy)`.

5. **Verify endpoints in browser or CLI:**
   - Frontend Landing Page: `http://localhost/`
   - System Health Dashboard: `http://localhost/health`
   - Backend Health API: `http://localhost/api/v1/health`
   - Database Health API: `http://localhost/api/v1/health/database`

6. **Stop containers safely (preserving data):**
   ```bash
   docker compose down
   ```
   > **Important:** Avoid `docker compose down -v` unless you intend to delete the `mafioso_mysql_data` and `mafioso_student_documents` named volumes.

## 3. Local Development Without Full Rebuild

### Backend (Java 8 + Maven)
```bash
cd backend
mvn -B clean test package
```
Output artifact: `backend/target/ROOT.war`.

### Frontend (React + Vite)
```bash
cd frontend
npm install
npm run dev       # Starts Vite dev server on :5173 with /api & /ws proxy to :8080
npm run build     # Produces optimized production bundle in frontend/dist/
```

## 4. Production Hardening Checklist

- Generate unique high-entropy passwords in `.env` for `DB_USER` and `DB_ROOT_PASSWORD`.
- Terminate HTTPS/TLS at the edge Nginx server block (ports `443` + HTTP-to-HTTPS redirect) and enable `DB_USE_SSL=true` if MySQL TLS is configured.
- Keep port `3306` and port `8080` closed to external networks.
- Schedule regular `mysqldump` backups of `mafioso_db` and offsite snapshots of `mafioso_student_documents`.
