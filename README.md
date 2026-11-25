# MAFIOSO — Study Abroad Consultancy Management Platform

MAFIOSO is a modular monolithic web platform supporting Bangladeshi students applying for BSc and MSc programmes at universities in the United Kingdom.

> **Current Status:** Milestone 2 — Authentication, Session Security, CSRF Protection, and Role-Based Authorization.
> University management, document workflows, SSLCOMMERZ payments, and live chat will be implemented in upcoming milestones.

## Technology Stack

- **Backend:** Java 8, Apache Tomcat 9, Servlet 4.0 (`javax.servlet.*`), JSR-356 WebSocket 1.1 (`javax.websocket.*`), JDBC, Jackson 2.18.3, Maven (WAR packaged as `ROOT.war`)
- **Frontend:** React 18, JavaScript, Vite 6, React Router 6, HTML, CSS
- **Database:** MySQL 8 (`mysql:8.0.46-oraclelinux9`)
- **Edge & Infrastructure:** Nginx (`nginx:1.28.3-alpine3.23`), Docker, Docker Compose, named persistent volumes

## Quick Start

```bash
cp .env.example .env
# Update DB_PASSWORD and DB_ROOT_PASSWORD inside .env
docker compose up --build -d
docker compose ps
```

Open `http://localhost` for the landing page and `http://localhost/health` for live system diagnostics.

## Key Endpoints (Same-Origin via Nginx)

| Path | Target | Description |
| --- | --- | --- |
| `/` | React SPA | MAFIOSO Landing Overview |
| `/health` | React SPA | Live Frontend, Backend, DB & WebSocket Diagnostics |
| `/api/v1/health` | Tomcat Servlet | Application liveness JSON response |
| `/api/v1/health/database` | Tomcat Servlet | Live MySQL `SELECT 1` connectivity & latency check |
| `/api/v1/auth/*` | Tomcat Servlet | CSRF, Student Registration, Login, Logout, and `/me` |
| `/api/v1/student/*` | Tomcat Servlet | Student-protected endpoints (`STUDENT`) |
| `/api/v1/consultant/*` | Tomcat Servlet | Staff-protected endpoints (`CONSULTANT`, `ADMIN`) |
| `/api/v1/admin/*` | Tomcat Servlet | Admin-protected endpoints (`ADMIN`) |
| `/ws/health` | Tomcat WebSocket | Milestone 1 WebSocket proxy handshake check |

## Documentation

- [Running MAFIOSO: Docker On/Off & Local Setup Without Docker](docs/LOCAL_AND_DOCKER_RUN_GUIDE.md)
- [Authentication, CSRF & Admin Bootstrap Guide](docs/AUTHENTICATION_AND_SECURITY.md)
- [Docker Setup Guide for New PCs](docs/DOCKER_SETUP_GUIDE.md)
- [Architecture Overview](docs/ARCHITECTURE.md)
- [Database Design & Backups](docs/DATABASE_DESIGN.md)
- [Development & Deployment Guide](docs/DEVELOPMENT_GUIDE.md)
- [Docker Infrastructure Details](docker/README.md)
- [Changelog](CHANGELOG.md)
