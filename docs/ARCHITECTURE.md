# MAFIOSO Platform Architecture

## 1. Overview

MAFIOSO is a study-abroad consultancy management platform initially serving students in Bangladesh pursuing BSc and MSc degrees in the United Kingdom. It follows a **modular monolithic architecture** without microservices or Spring Boot.

```text
Browser (Same Origin :80)
   │
   ▼
[Nginx:1.28.3-alpine3.23]  (frontend container, host port 80)
   ├── /                   ──▶ React + Vite Static SPA (/index.html fallback)
   ├── /api/*              ──▶ HTTP/1.1 Reverse Proxy ──▶ [Tomcat 9 + Java 8] (/api/v1/*)
   └── /ws/*               ──▶ WebSocket Upgrade Proxy ──▶ [Tomcat 9 + Java 8] (/ws/*)
                                                                 │
                                                                 ├── JDBC (db:3306) ──▶ [MySQL 8.0.46]
                                                                 └── Private Volume  ──▶ /var/mafioso/storage/documents
```

## 2. Backend Modular Monolith (`com.mafioso`)

Built with Java 8, Servlet 4.0 (`javax.servlet.*`), JSR-356 WebSocket 1.1 (`javax.websocket.*`), JDBC, and Jackson 2.18.x, packaged as `ROOT.war`:

- `config`: Environment-driven configuration (`AppConfig`, `DatabaseConfig`)
- `controller`: HTTP Servlet endpoints (`HealthServlet`, `DatabaseHealthServlet`)
- `service`: Business orchestration & transaction boundaries (`HealthCheckService`)
- `dao`: JDBC data access layer (`HealthCheckDao`)
- `model`: Future domain entities
- `dto`: JSON response DTOs (`HealthResponse`, `DatabaseHealthResponse`)
- `filter`: Cross-cutting Servlet filters (`CharacterEncodingFilter`)
- `websocket`: Tomcat 9 JSR-356 WebSocket endpoints (`HealthWebSocketEndpoint`, future `/ws/chat`)
- `payment`: Future SSLCOMMERZ payment integration adapters
- `util`: Shared JDBC and Jackson helpers (`DatabaseUtil`, `JsonUtil`)

## 3. Frontend Architecture

Built with React, JavaScript, Vite, and React Router:

- `src/config/env.js`: Public non-secret runtime metadata
- `src/services/apiClient.js`: Same-origin `fetch` client using relative `/api/...` URLs
- `src/services/healthService.js`: Diagnostics probes for REST API, MySQL health, and WebSocket proxy
- `src/pages/LandingPage.jsx`: Initial MAFIOSO platform overview
- `src/pages/HealthCheckPage.jsx`: Live stack health diagnostic console
- `src/pages/NotFoundPage.jsx`: React Router fallback view

## 4. Development vs. Production Topology

### Development
- Local Docker Compose environment (`APP_ENV=development`)
- Direct Maven (`mvn clean package`) and Vite (`npm run dev`) support for rapid iteration
- Unencrypted internal container traffic between Nginx, Tomcat, and MySQL on `mafioso_internal`

### Production
- Optimized Vite static build served by Nginx with immutable asset caching
- Compiled `ROOT.war` deployed inside Tomcat 9
- TLS/HTTPS termination at the Nginx edge (or upstream TLS load balancer) forwarding `X-Forwarded-Proto: https`
- Strong generated credentials in `.env` (never committed), private-only MySQL access, persistent volumes, and scheduled database backups
