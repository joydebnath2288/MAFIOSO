# MAFIOSO — Study Abroad Consultancy Management Platform

MAFIOSO is a modular monolithic web platform supporting Bangladeshi students applying for BSc and MSc programmes at universities in the United Kingdom.

> **Current Status:** Milestone 1 — Complete Project Foundation and Dockerization.
> Business features (authentication, dashboards, university management, document workflows, SSLCOMMERZ payments, and live chat) will be implemented in upcoming milestones.

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
| `/ws/health` | Tomcat WebSocket | Milestone 1 WebSocket proxy handshake check |

## Documentation

- [Architecture Overview](docs/ARCHITECTURE.md)
- [Database Design & Backups](docs/DATABASE_DESIGN.md)
- [Development & Deployment Guide](docs/DEVELOPMENT_GUIDE.md)
- [Docker Infrastructure Details](docker/README.md)
- [Changelog](CHANGELOG.md)
